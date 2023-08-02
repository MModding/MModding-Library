package com.mmodding.mmodding_lib.mixin.injectors;

import com.mmodding.mmodding_lib.ducks.EntityDuckInterface;
import com.mmodding.mmodding_lib.ducks.PortalForcerDuckInterface;
import com.mmodding.mmodding_lib.ducks.ServerPlayerDuckInterface;
import com.mmodding.mmodding_lib.library.portals.CustomSquaredPortalBlock;
import com.mmodding.mmodding_lib.library.utils.WorldUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.AreaHelper;
import net.minecraft.world.dimension.DimensionType;
import org.quiltmc.qsl.worldgen.dimension.api.QuiltDimensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityDuckInterface {

	@Unique
	boolean inCustomPortal;

	@Unique
	int customPortalTime;

	@Unique
	boolean useCustomPortalElements;

	@Unique
	Pair<Block, CustomSquaredPortalBlock> customPortalElements;

	@Unique
	BlockPos lastCustomPortalPosition;

	@Unique
	CustomSquaredPortalBlock customPortalCache;

	@Override
	public boolean isInCustomPortal() {
		return this.inCustomPortal;
	}

	@Override
	public Pair<Block, CustomSquaredPortalBlock> getCustomPortalElements() {
		return this.customPortalElements;
	}

	@Override
	public CustomSquaredPortalBlock getCustomPortalCache() {
		return this.customPortalCache;
	}

	@Shadow
	public abstract double squaredDistanceTo(Entity entity);

	@Shadow
	public abstract World getWorld();

	@Shadow
	public abstract boolean hasNetherPortalCooldown();

	@Shadow
	public abstract void resetNetherPortalCooldown();

	@Shadow
	public abstract int getMaxNetherPortalTime();

	@Shadow
	public abstract boolean hasVehicle();

	@Shadow
	protected abstract void tickNetherPortalCooldown();

	@Shadow
	public abstract double getX();

	@Shadow
	public abstract double getY();

	@Shadow
	public abstract double getZ();

	@Shadow
	protected abstract Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect);

	@Shadow
	public abstract EntityDimensions getDimensions(EntityPose pose);

	@Shadow
	public abstract EntityPose getPose();

	@Shadow
	public abstract Vec3d getVelocity();

	@Shadow
	public abstract float getYaw();

	@Shadow
	public abstract float getPitch();

	@Shadow
	@Final
	protected RandomGenerator random;

	@Shadow
	public float fallDistance;

	@Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tickNetherPortal()V", shift = At.Shift.AFTER))
	private void baseTickAfterTickNetherPortal(CallbackInfo ci) {
		this.tickCustomPortal();
	}

	@Unique
	public void setInCustomPortal(Block frameBlock, CustomSquaredPortalBlock portalBlock, World world, BlockPos pos) {
		if (this.hasNetherPortalCooldown()) {
			this.resetNetherPortalCooldown();
		}
		else {
			if (!this.getWorld().isClient() && !pos.equals(this.lastCustomPortalPosition)) {
				this.lastCustomPortalPosition = pos.toImmutable();
			}

			this.inCustomPortal = true;
			this.customPortalElements = new Pair<>(frameBlock, portalBlock);

			if (world instanceof ClientWorld clientWorld) {
				Entity thisEntity = ((Entity) (Object) this);
				if (thisEntity instanceof ClientPlayerEntity playerEntity) {
					this.customPortalCache = portalBlock;
					WorldUtils.repeatTaskUntil(clientWorld, 39, () -> this.customPortalCache = playerEntity.lastNauseaStrength > 0 ? portalBlock : null);
					WorldUtils.doTaskAfter(clientWorld, 40, () -> this.customPortalCache = null);
				}
			}
		}
	}

	@Unique
	public void tickCustomPortal() {
		if (this.getWorld() instanceof ServerWorld serverWorld) {
			int i = this.getMaxNetherPortalTime();
			if (this.inCustomPortal) {
				MinecraftServer minecraftServer = serverWorld.getServer();
				RegistryKey<World> portalWorldKey = this.customPortalElements.getSecond().getWorldKey();
				RegistryKey<World> registryKey = serverWorld.getRegistryKey() == portalWorldKey ? World.OVERWORLD : portalWorldKey;
				ServerWorld destinationWorld = minecraftServer.getWorld(registryKey);

				if (destinationWorld != null && !this.hasVehicle() && this.customPortalTime++ >= i) {
					this.getWorld().getProfiler().push("portal");
					this.customPortalTime = i;
					this.resetNetherPortalCooldown();
					QuiltDimensions.teleport((Entity) (Object) this, destinationWorld, this.getCustomPortalTarget(destinationWorld));
					this.getWorld().getProfiler().pop();
				}

				this.inCustomPortal = false;
			}
			else {
				if (this.customPortalTime > 0) {
					this.customPortalTime -= 4;
				}

				if (this.customPortalTime < 0) {
					this.customPortalTime = 0;
				}
			}
		}
	}

	@Unique
	private TeleportTarget getCustomPortalTarget(ServerWorld destination) {
		WorldBorder worldBorder = destination.getWorldBorder();
		double coordScaleFactor = DimensionType.getCoordinateScaleFactor(this.getWorld().getDimension(), destination.getDimension());
		BlockPos posFactorScaled = worldBorder.method_39538(this.getX() * coordScaleFactor, this.getY(), this.getZ() * coordScaleFactor);
		this.useCustomPortalElements = true;

		Function<BlockLocating.Rectangle, TeleportTarget> func = rectangle -> {
			BlockState lastCustomPortalState = this.getWorld().getBlockState(this.lastCustomPortalPosition);
			Direction.Axis axisTarget;
			Vec3d vec3dTarget;

			if (lastCustomPortalState.contains(Properties.HORIZONTAL_AXIS)) {
				axisTarget = lastCustomPortalState.get(Properties.HORIZONTAL_AXIS);
				BlockLocating.Rectangle rectangle2 = BlockLocating.getLargestRectangle(
					this.lastCustomPortalPosition,
					axisTarget,
					21,
					Direction.Axis.Y,
					21,
					pos -> this.getWorld().getBlockState(pos) == lastCustomPortalState
				);
				vec3dTarget = this.positionInPortal(axisTarget, rectangle2);
			} else {
				axisTarget = Direction.Axis.X;
				vec3dTarget = new Vec3d(0.5, 0.0, 0.0);
			}

			return AreaHelper.getNetherTeleportTarget(
				destination, rectangle, axisTarget, vec3dTarget, this.getDimensions(this.getPose()), this.getVelocity(), this.getYaw(), this.getPitch()
			);
		};

		Entity thisEntity = (Entity) (Object) this;

		if (thisEntity instanceof ServerPlayerEntity player) {
			return ((ServerPlayerDuckInterface) player).getCustomPortalRect(destination, posFactorScaled, worldBorder).map(func).orElse(null);
		}
		else {
			return ((PortalForcerDuckInterface) destination.getPortalForcer()).searchCustomPortal(this.customPortalElements.getSecond().getPoiKey(), posFactorScaled, worldBorder).map(func).orElse(null);
		}
	}
}
