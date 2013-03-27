package com.github.catageek.ByteCart.Signs;

/**
 * Match IP ranges and negate the result.
 *
 * 1. Empty
 * 2. [BC9337]
 * 3. AA.BB.CC
 * 4. XX.YY.ZZ
 *
 * onState <=> !(AA.BB.CC <= IP <= XX.YY.ZZ)
 */
public class BC9137 extends BC9037 {

	public BC9137(org.bukkit.block.Block block, org.bukkit.entity.Vehicle vehicle) {
		super(block, vehicle);
	}

	@Override
	protected boolean negated() {
		return true;
	}

	@Override
	public final String getName() {
		return "BC9137";
	}

	@Override
	public final String getFriendlyName() {
		return "Negated range matcher";
	}

}
