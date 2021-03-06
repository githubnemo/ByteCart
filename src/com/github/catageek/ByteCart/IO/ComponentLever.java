package com.github.catageek.ByteCart.IO;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.Lever;

import com.github.catageek.ByteCart.Util.MathUtil;

public class ComponentLever extends AbstractComponent implements OutputPin, InputPin {

	public ComponentLever(Block block) {
		super(block.getLocation());
		/*		if(ByteCart.debug)
			ByteCart.log.info("ByteCart : adding Lever at " + block.getLocation().toString());
		 */
	}

	@Override
	public void write(boolean bit) {
		if(this.getLocation().getBlock().getState().getData() instanceof Lever) {
			Lever lever = new Lever(Material.LEVER, this.getLocation().getBlock().getData());
			if(lever.isPowered()^bit) {
				lever.setPowered(bit);
				this.getLocation().getBlock().setData(lever.getData(), true);
			}
			/*			
			if(ByteCart.debug)
				ByteCart.log.info("Lever at (" + this.getLocation().toString() + ") : " + bit);
			 */

			MathUtil.forceUpdate(this.getLocation().getBlock().getRelative(lever.getAttachedFace()));
		}
	}

	@Override
	public boolean read() {
		if(this.getLocation().getBlock().getState().getData() instanceof Lever) {
			final Lever lever = new Lever(Material.LEVER, this.getLocation().getBlock().getData());
			return lever.isPowered();

		}
		return false;
	}


}
