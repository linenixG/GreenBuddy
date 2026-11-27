package com.example.greenbuddy;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob; // <--- ВОТ ОН, НЕДОСТАЮЩИЙ ИМПОРТ
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class GreenBuddyEntity extends PathfinderMob {
    public boolean isAggressive = false;
    private int chopCooldown = 0;

    public GreenBuddyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    public void tick() {
        super.tick();
        
        if (this.isAggressive && this.tickCount % 20 == 0) {
            List<Monster> monsters = this.level().getEntitiesOfClass(Monster.class, new AABB(this.blockPosition()).inflate(16.0D));
            if (!monsters.isEmpty()) {
                this.setTarget(monsters.get(0));
            }
        }

        if (!this.isAggressive && this.chopCooldown <= 0) {
            BlockPos targetPos = findNearbyLog();
            if (targetPos != null) {
                double dx = targetPos.getX() - this.getX();
                double dz = targetPos.getZ() - this.getZ();
                double dist = Math.sqrt(dx*dx + dz*dz);
                if (dist < 3.0) {
                    this.level().destroyBlock(targetPos, true);
                    this.chopCooldown = 100;
                } else {
                    this.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
                }
            }
        } else {
            this.chopCooldown--;
        }
    }

    private BlockPos findNearbyLog() {
        BlockPos center = this.blockPosition();
        for (int x = -10; x <= 10; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (this.level().getBlockState(pos).is(BlockTags.LOGS)) return pos;
                }
            }
        }
        return null;
    }
}
