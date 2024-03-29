package com.mmodding.mmodding_lib.library.network.support;

import com.mmodding.mmodding_lib.library.utils.ShouldNotUse;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public interface NetworkSupport {

	Map<Identifier, Class<? extends NetworkSupport>> REGISTRY = new HashMap<>();
	Map<Class<? extends NetworkSupport>, Function<PacketByteBuf, ? extends NetworkSupport>> MANAGER = new HashMap<>();

	static <T extends NetworkSupport> void register(Identifier identifier, Class<T> type, Function<PacketByteBuf, T> function) {
		REGISTRY.put(identifier, type);
		MANAGER.put(type, function);
	}

	@SuppressWarnings("unchecked")
	static <T extends NetworkSupport> Class<T> getType(PacketByteBuf buf) {
		PacketByteBuf copied = new PacketByteBuf(buf.copy());
		Identifier identifier = copied.readIdentifier();
		if (REGISTRY.containsKey(identifier)) {
			return (Class<T>) REGISTRY.get(identifier);
		}
		throw new IllegalArgumentException("Identifier is not present in the NetworkSupport Registry");
	}

	static <T extends NetworkSupport> T readCompleteAsNullable(PacketByteBuf buf) {
		return buf.readNullable(NetworkSupport::readComplete);
	}

	@SuppressWarnings("unchecked")
	static <T extends NetworkSupport> T readComplete(PacketByteBuf buf) {
		PacketByteBuf check = new PacketByteBuf(buf.copy());
		Identifier identifier = check.readIdentifier();
		if (REGISTRY.containsKey(identifier)) {
			buf.readIdentifier();
			Class<? extends NetworkSupport> type = REGISTRY.get(identifier);
			return (T) MANAGER.get(type).apply(buf);
		}
		throw new IllegalArgumentException("Identifier is not present in the NetworkSupport Registry");
	}

	static <T extends NetworkSupport> void writeCompleteAsNullable(T value,  PacketByteBuf buf) {
		buf.writeNullable(value, (current, support) -> support.writeComplete(current));
	}

	default void writeComplete(PacketByteBuf buf) {
		if (REGISTRY.containsValue(this.getClass())) {
			Stream<Identifier> identifiers = REGISTRY.entrySet().stream().filter(entry -> this.getClass().equals(entry.getValue())).map(Map.Entry::getKey);
			Optional<Identifier> identifier = identifiers.findFirst();
			if (identifier.isPresent()) {
				buf.writeIdentifier(identifier.get());
				this.write(buf);
				return;
			}
		}
		throw new IllegalArgumentException("Class is not present in the NetworkSupport Registry");
	}

	@ShouldNotUse(useInstead = "writeComplete")
	void write(PacketByteBuf buf);
}
