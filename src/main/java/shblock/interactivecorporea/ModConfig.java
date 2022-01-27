package shblock.interactivecorporea;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class ModConfig {
  private static class Client {
    public Client(ForgeConfigSpec.Builder builder) {

    }
  }

  public static final Client CLIENT;
  public static final ForgeConfigSpec CLIENT_SPEC;
  static {
    final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
    CLIENT_SPEC = specPair.getRight();
    CLIENT = specPair.getLeft();
  }

  public static class Common {
    public final ForgeConfigSpec.IntValue requestingHaloStaticConsumption;
    public final ForgeConfigSpec.IntValue requestingHaloUpdateConsumption;
    public final ForgeConfigSpec.IntValue quantizationConsumption;
    public final ForgeConfigSpec.IntValue quantizationDeviceManaCapacity;

    public Common(ForgeConfigSpec.Builder builder) {
      builder.push("Mana");

      requestingHaloStaticConsumption = builder
          .comment("Mana consumption per tick while the halo interface is open")
          .defineInRange("requestingHaloStaticConsumption", 1, 0, 100);
      requestingHaloUpdateConsumption = builder
          .comment("Mana consumption when the displayed item list is updated")
          .defineInRange("requestingHaloUpdateConsumption", 10, 0, 100);

      quantizationConsumption = builder
          .comment("Mana consumption PER ITEM to quantize items")
          .defineInRange("quantizationConsumption", 20, 0, 100);
      quantizationDeviceManaCapacity = builder
          .comment("The mana capacity of the Quantization Device (recommended to be larger than <quantizationConsumption> * 256)")
          .defineInRange("quantizationDeviceManaCapacity", 10000, 1, Integer.MAX_VALUE);

      builder.pop();
    }
  }

  public static final Common COMMON;
  public static final ForgeConfigSpec COMMON_SPEC;
  static {
    final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
    COMMON_SPEC = specPair.getRight();
    COMMON = specPair.getLeft();
  }
}
