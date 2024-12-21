package me.kopamed.galacticc.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class debian {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static List<EntityLivingBase> getTargets(double reach) {
        List<EntityLivingBase> targets = new ArrayList<>();

        for (Entity ent : mc.world.loadedEntityList) {
            if (ent instanceof EntityLivingBase) {
                EntityLivingBase entityLiving = (EntityLivingBase) ent;
                if (!entityLiving.isDead && entityLiving.getHealth() > 0 && entityLiving != mc.player && entityLiving.getDistance(mc.player) < reach) {
                    targets.add(entityLiving);
                }
            }
        }

        return targets;
    }

    public static List<EntityLivingBase> sortByRange(List<EntityLivingBase> entities) {
        List<EntityLivingBase> targetsSorted = new ArrayList<>();

        while (targetsSorted.size() < entities.size()) {
            int index = 0;
            double smallestDistance = -1;

            for (int ind = 0; ind < entities.size(); ind++) {
                EntityLivingBase elb = entities.get(ind);
                double distanceToPlayer = elb.getDistance(mc.player);

                if (smallestDistance == -1 || distanceToPlayer < smallestDistance) {
                    smallestDistance = distanceToPlayer;
                    index = ind;
                }
            }

            targetsSorted.add(entities.get(index));
            entities.remove(index); // Avoid duplicates
        }

        return targetsSorted;
    }

    public static ArrayList<EntityPlayer> getPlayers(List<EntityLivingBase> entities, double reach) {
        ArrayList<EntityPlayer> targets = new ArrayList<>();

        for (Entity ent : entities) {
            if (ent instanceof EntityPlayer) {
                if (ent != mc.player && ent.getDistance(mc.player) < reach) {
                    targets.add((EntityPlayer) ent);
                }
            }
        }

        return targets;
    }

    public static float[] getRotations(Entity e) {
        double deltaX = e.posX + (e.posX - e.lastTickPosX) - mc.player.posX;
        double deltaZ = e.posZ + (e.posZ - e.lastTickPosZ) - mc.player.posZ;
        double deltaY = e.posY - 3.5 + e.getEyeHeight() - mc.player.posY + mc.player.getEyeHeight();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ));
        float pitch = (float) -Math.toDegrees(Math.atan(deltaY / distance));

        if (deltaX < 0 && deltaZ < 0) {
            yaw = (float) (90 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        } else if (deltaX > 0 && deltaZ < 0) {
            yaw = (float) (-90 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }

        return new float[]{yaw, pitch};
    }
}
