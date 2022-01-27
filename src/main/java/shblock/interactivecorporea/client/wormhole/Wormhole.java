package shblock.interactivecorporea.client.wormhole;

import vazkii.botania.common.core.helper.Vector3;

public class Wormhole {
  public Vector3 pos;
  public Vector3 normal;
  public double radius;

  public Wormhole(Vector3 pos, Vector3 normal, double radius) {
    this.pos = pos;
    this.normal = normal;
    this.radius = radius;
  }
}
