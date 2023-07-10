package lol.magix.breakingbedrock.objects.binary;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.nbt.NbtList;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;

import java.util.List;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NbtMapOps implements DynamicOps<Object> {
    public static final NbtMapOps INSTANCE = new NbtMapOps();

    @Override
    public Object empty() {
        return null;
    }

    @Override
    public Object emptyMap() {
        return NbtMap.EMPTY;
    }

    @Override
    public Object emptyList() {
        return NbtList.EMPTY;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, Object input) {
        if(input == null) {
            return outOps.empty();
        }
        if(input instanceof NbtMap || input instanceof NbtMapBuilder) {
            return convertMap(outOps, input);
        }
        if(input instanceof NbtList<?>) {
            return convertList(outOps, input);
        }
        if(input instanceof String str) {
            return outOps.createString(str);
        }
        if(input instanceof Boolean bool) {
            return outOps.createBoolean(bool);
        }
        if(input instanceof Number num) {
            if (input instanceof Byte) {
                return outOps.createByte(num.byteValue());
            }
            if (input instanceof Short) {
                return outOps.createShort(num.shortValue());
            }
            if (input instanceof Integer) {
                return outOps.createInt(num.intValue());
            }
            if (input instanceof Long) {
                return outOps.createLong(num.longValue());
            }
            if (input instanceof Double) {
                return outOps.createDouble(num.doubleValue());
            }
            if (input instanceof Float) {
                return outOps.createFloat(num.floatValue());
            }
        }
        return outOps.empty();
    }

    @Override
    public DataResult<Number> getNumberValue(Object input) {
        if(input instanceof Number num) {
            return DataResult.success(num);
        }
        if(input instanceof Boolean bool) {
            return DataResult.success(bool ? 1 : 0);
        }

        return DataResult.error(() -> "Not a number: " + input);
    }

    @Override
    public Object createNumeric(Number i) {
        return createDouble(i.doubleValue());
    }

    @Override
    public Object createByte(byte value) {
        return value;
    }

    @Override
    public Object createShort(short value) {
        return value;
    }

    @Override
    public Object createInt(int value) {
        return value;
    }

    @Override
    public Object createLong(long value) {
        return value;
    }

    @Override
    public Object createFloat(float value) {
        return value;
    }

    @Override
    public Object createDouble(double value) {
        return value;
    }

    @Override
    public Object createString(String value) {
        return value;
    }

    @Override
    public DataResult<String> getStringValue(Object input) {
        if (input instanceof String str) {
            return DataResult.success(str);
        }
        return DataResult.error(() -> "Not a string");
    }

    @Override
    public DataResult<Object> mergeToList(Object list, Object value) {
        if (list != empty()) {
            return DataResult.error(() -> "mergeToList called with empty: " + list, list);
        }
        if (!(list instanceof NbtList<?> nbtArray)) {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }
        if (!nbtArray.getType().getTagClass().equals(value.getClass())) {
            return DataResult.error(() -> "mergeToList called with incompatible value: " + value, list);
        }
        NbtList<Object> nbtList = (NbtList<Object>) nbtArray;

        NbtList<Object> result = new NbtList<>(nbtList.getType());
        if (nbtList != empty()) {
            result.addAll(nbtList);
        }
        result.add(value);

        return DataResult.success(result);
    }

    @Override
    public DataResult<Object> mergeToMap(Object map, Object key, Object value) {
        if (map != empty()) {
            return DataResult.error(() -> "mergeToMap called with empty: " + map, map);
        }
        if (!(key instanceof String strKey)) {
            return DataResult.error(() -> "key is not a string: " + key, map);
        }
        NbtMapBuilder builder = NbtMap.builder();
        if (map instanceof NbtMap nbtMap) {
            if (map != empty()) {
                builder.putAll(nbtMap);
            }
        }else if (map instanceof NbtMapBuilder builder1) {
            if (map != empty()) {
                builder.putAll(builder1);
            }
        }else{
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }

        builder.put(strKey, value);
        return DataResult.success(builder.build());
    }

    @Override
    public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object input) {
        if(input instanceof NbtMap map) {
            return DataResult.success(map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())));
        }
        if(input instanceof NbtMapBuilder builder) {
            return DataResult.success(builder.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())));
        }
        return DataResult.error(() -> "Not a map: " + input);
    }

    @Override
    public Object createMap(Stream<Pair<Object, Object>> map) {
        NbtMapBuilder builder = NbtMap.builder();
        map.forEach(pair -> builder.put(pair.getFirst().toString(), pair.getSecond()));
        return builder.build();
    }

    @Override
    public DataResult<Stream<Object>> getStream(Object input) {
        if(input instanceof NbtList<?> list) {
            return DataResult.success(((NbtList<Object>) list).stream());
        }

        return DataResult.error(() -> "Not a list");
    }

    @Override
    public Object createList(Stream<Object> input) {
        List<Object> list = input.toList();
        if(list.isEmpty()) {
            return emptyList();
        }
        Object first = list.get(0);
        if(!list.stream().allMatch(o -> first.getClass().equals(o.getClass()))) {
            return emptyList();
        }

        return new NbtList(NbtType.byClass(first.getClass()), list);
    }

    @Override
    public Object remove(Object input, String key) {
        if(input instanceof NbtMap map) {
            NbtMapBuilder builder = map.toBuilder();
            builder.remove(key);
            return builder.build();
        }
        if(input instanceof NbtMapBuilder builder) {
            builder.remove(key);
            return builder.build();
        }
        return null;
    }
}
