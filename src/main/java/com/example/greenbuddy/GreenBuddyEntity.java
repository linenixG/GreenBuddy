package com.example.greenbuddy;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class GreenBuddyEntity extends PathfinderMob {
    public int attackCooldown = 0;

    public GreenBuddyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Умный бой с дистанцией (вместо обычного MeleeAttackGoal)
        this.goalSelector.addGoal(1, new SmartCombatGoal(this, 1.2D));
        // Подбор предметов (мечей, еды и т.д.)
        this.goalSelector.addGoal(2, new SmartPickupGoal(this, 1.0D));
        // Рубка деревьев, если нет врагов
        this.goalSelector.addGoal(3, new SmartChopTreeGoal(this, 1.0D, 16, 4));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // Атакуем всех монстров (кроме криперов)
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, 
            (entity) -> !entity.getType().toString().contains("creeper")));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.attackCooldown > 0) this.attackCooldown--;
    }

    // Умный бой: подойти, ударить и отойти
    static class SmartCombatGoal extends Goal {
        private final GreenBuddyEntity buddy;
        private final double speed;
        private int retreatTicks = 0;
        private int attackDelay = 0;

        public SmartCombatGoal(GreenBuddyEntity buddy, double speed) {
            this.buddy = buddy;
            this.speed = speed;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.buddy.getTarget() != null && this.buddy.getTarget().isAlive();
        }

        @Override
        public void start() {
            this.retreatTicks = 0;
            this.attackDelay = 0;
        }

        @Override
        public void tick() {
            net.minecraft.world.entity.LivingEntity target = this.buddy.getTarget();
            if (target == null) return;

            double dist = this.buddy.distanceTo(target);

            // Смотрим на врага
            this.buddy.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Если мы отступаем после удара
            if (this.retreatTicks > 0) {
                this.retreatTicks--;
                // Отходим назад
                Vec3 dir = new Vec3(this.buddy.getX() - target.getX(), 0, this.buddy.getZ() - target.getZ()).normalize();
                this.buddy.getNavigation().moveTo(this.buddy.getX() + dir.x * 2, this.buddy.getY(), this.buddy.getZ() + dir.z * 2, this.speed);
                return;
            }

            // Держим дистанцию
            if (dist > 2.5D) {
                // Подходим
                this.buddy.getNavigation().moveTo(target, this.speed);
            } else {
                // Мы близко, останавливаемся
                this.buddy.getNavigation().stop();
                
                // Атакуем с задержкой (как игрок)
                if (this.attackDelay <= 0 && this.buddy.attackCooldown <= 0) {
                    // Проверяем, есть ли у него в руке меч, чтобы нанести больше урона
                    ItemStack heldItem = this.buddy.getItemInHand(InteractionHand.MAIN_HAND);
                    float damage = 4.0F;
                    if (heldItem.getItem() instanceof SwordItem sword) {
                        damage = sword.getDamage();
                    }
                    
                    // Бьем
                    target.hurt(this.buddy.damageSources().mobAttack(this.buddy), damage);
                    this.buddy.swing(InteractionHand.MAIN_HAND); // Анимация руки!
                    
                    this.attackDelay = 20; // 1 секунда до следующего удара
                    this.retreatTicks = 10; // Отступаем на 0.5 секунды после удара
                    this.buddy.attackCooldown = 20;
                }
            }

            if (this.attackDelay > 0) this.attackDelay--;
        }
    }

    // Подбор мечей
    static class SmartPickupGoal extends Goal {
        private final GreenBuddyEntity buddy;
        private final double speed;
        private ItemEntity targetItem = null;

        public SmartPickupGoal(GreenBuddyEntity buddy, double speed) {
            this.buddy = buddy;
            this.speed = speed;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.buddy.getTarget() != null) return false; // Не подбираем, если идет бой
            
            List<ItemEntity> items = this.buddy.level().getEntitiesOfClass(ItemEntity.class, new AABB(this.buddy.blockPosition()).inflate(10.0D));
            for (ItemEntity item : items) {
                if (item.getItem().getItem() instanceof SwordItem && this.buddy.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                    this.targetItem = item;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void tick() {
            if (this.targetItem != null && this.targetItem.isAlive()) {
                this.buddy.getNavigation().moveTo(this.targetItem, this.speed);
                if (this.buddy.distanceTo(this.targetItem) < 1.5D) {
                    // Подбираем предмет в руку
                    this.buddy.setItemInHand(InteractionHand.MAIN_HAND, this.targetItem.getItem().copy());
                    this.targetItem.discard(); // Удаляем предмет с земли
                }
            }
        }
    }

    static class SmartChopTreeGoal extends MoveToBlockGoal {
        private final GreenBuddyEntity buddy;

        public SmartChopTreeGoal(GreenBuddyEntity buddy, double speed, int searchRange, int verticalSearchRange) {
            super(buddy, speed, searchRange, verticalSearchRange);
            this.buddy = buddy;
        }

        @Override
        protected boolean isValidTarget(net.minecraft.world.level.LevelReader level, BlockPos pos) {
            return level.getBlockState(pos).is(BlockTags.LOGS);
        }

        @Override
        public void tick() {
            super.tick();
            if (this.isReachedTarget()) {
                BlockPos pos = this.blockPos;
                BlockState state = this.buddy.level().getBlockState(pos);
                if (state.is(BlockTags.LOGS)) {
                    this.buddy.level().destroyBlock(pos, false);
                    this.buddy.getNavigation().stop();
                    this.nextStartTick = 40;
                }
            }
        }
    }
}
