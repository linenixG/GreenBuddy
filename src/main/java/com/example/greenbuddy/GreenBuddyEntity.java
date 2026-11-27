package com.example.greenbuddy;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class GreenBuddyEntity extends PathfinderMob {
    public boolean isAggressive = false;
    public boolean isFollowing = false;
    public boolean isBuilding = false;
    public int woodCollected = 0;
    public int buildCooldown = 0;
    public BlockPos guardPos = null;

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
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new SmartChopTreeGoal(this, 1.0D, 16, 4));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, 
            (entity) -> !(entity instanceof Creeper) && this.isAggressive));
    }

    @Override
    public void tick() {
        super.tick();
        
        // Логика следования
        if (this.isFollowing && this.getTarget() == null) {
            Player player = this.level().getNearestPlayer(this, 20.0D);
            if (player != null && this.distanceTo(player) > 3.0F) {
                this.getNavigation().moveTo(player, 1.1D);
            }
        }

        // Логика охраны
        if (this.guardPos != null && this.getTarget() == null) {
            if (this.distanceToSqr(this.guardPos.getX(), this.guardPos.getY(), this.guardPos.getZ()) > 4.0D) {
                this.getNavigation().moveTo(this.guardPos.getX(), this.guardPos.getY(), this.guardPos.getZ(), 1.0D);
            }
        }

        // Логика строительства
        if (this.isBuilding && this.buildCooldown <= 0) {
            buildHouse();
        }
        if (this.buildCooldown > 0) {
            this.buildCooldown--;
        }
    }

    private void buildHouse() {
        BlockPos center = this.blockPosition();
        // Строим коробку 3x3 из досок
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 2; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (this.level().getBlockState(pos).isAir()) {
                        if (y == 0 || (x == -1 || x == 1 || z == -1 || z == 1)) {
                            this.level().setBlockAndUpdate(pos, Blocks.OAK_PLANKS.defaultBlockState());
                        }
                    }
                }
            }
        }
        // Крыша
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                this.level().setBlockAndUpdate(center.offset(x, 3, z), Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
        this.isBuilding = false;
        this.buildCooldown = 200;
    }

    // Лечение
    public void healBuddy() {
        this.setHealth(this.getMaxHealth());
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1, this.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
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
                    this.buddy.woodCollected++;
                    this.buddy.spawnAtLocation(new ItemStack(Items.OAK_LOG), 1.0F);
                    this.buddy.getNavigation().stop();
                    this.nextStartTick = 40;
                }
            }
        }
    }
}
