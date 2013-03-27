package com.github.catageek.ByteCart.Signs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;


import com.github.catageek.ByteCart.ByteCart;
import com.github.catageek.ByteCart.CollisionManagement.SimpleCollisionAvoider;
import com.github.catageek.ByteCart.CollisionManagement.SimpleCollisionAvoider.Side;
import com.github.catageek.ByteCart.Event.SignPostStationEvent;
import com.github.catageek.ByteCart.Event.SignPreStationEvent;
import com.github.catageek.ByteCart.HAL.PinRegistry;
import com.github.catageek.ByteCart.HAL.RegistryBoth;
import com.github.catageek.ByteCart.HAL.RegistryInput;
import com.github.catageek.ByteCart.IO.InputPin;
import com.github.catageek.ByteCart.IO.InputFactory;
import com.github.catageek.ByteCart.Routing.Address;
import com.github.catageek.ByteCart.Routing.AddressFactory;
import com.github.catageek.ByteCart.Routing.Updater;
import com.github.catageek.ByteCart.Routing.UpdaterFactory;
import com.github.catageek.ByteCart.Util.MathUtil;



/**
 * Match IP ranges.
 *
 * 1. Empty
 * 2. [BC9337]
 * 3. AA.BB.CC
 * 4. XX.YY.ZZ
 *
 * onState <=> AA.BB.CC <= IP <= XX.YY.ZZ
 */
public class BC9037 extends AbstractBC9000 implements Subnet, Powerable, Triggable {

	public BC9037(org.bukkit.block.Block block, org.bukkit.entity.Vehicle vehicle) {
		super(block, vehicle);
		this.netmask = 4;
	}

	/**
	 *
	 * @return Range start address from the third line.
	 */
	protected Address getRangeStartAddress() {
		return AddressFactory.getAddress(getBlock(), 2);
	}

	/**
	 * @return Range end address from the fourth line.
	 */
	protected Address getRangeEndAddress() {
		return AddressFactory.getAddress(getBlock(), 3);
	}

	protected Address getVehicleAddress() {
		return AddressFactory.getAddress(this.getInventory());
	}

	protected void addAddressAsInputs(Address addr) {
		if(addr.isValid()) {
			RegistryInput region = addr.getRegion();
			this.addInputRegistry(region);

			RegistryInput track = addr.getTrack();
			this.addInputRegistry(track);

			RegistryBoth station = addr.getStation();
			this.addInputRegistry(station);
		}
	}

	/**
	 * The range start address is already in port 4,5,6 (region,track,station) by
	 * super.addIO(), but the station address is broken due to application of the net
	 * mask to the value.
	 *
	 * Thus, we add the range start address to 8,9,A (region, track, station) and
	 * the range end address to B,C,D with the same configuration, without the net masks
	 * applied on the station numbers.
	 *
	 * The same goes for the IP address of the vehicle. The corresponding sections
	 * are added to indices E, F and 10.
	 */
	@Override
	protected void addIO() {
		super.addIO();

		/* TODO: remove
		addAddressAsInputs(getRangeStartAddress());
		addAddressAsInputs(getRangeEndAddress());
		addAddressAsInputs(AddressFactory.getAddress(this.getInventory()));
		*/
	}

	@Override
	public void trigger() {
		try {
			this.addIO();

			InputPin[] wire = new InputPin[2];

			// TODO: remove bc7003 code

			// Right
			wire[0] = InputFactory.getInput(this.getBlock().getRelative(BlockFace.UP).getRelative(getCardinal(), 2).getRelative(MathUtil.clockwise(getCardinal())));
			// left
			wire[1] = InputFactory.getInput(this.getBlock().getRelative(BlockFace.UP).getRelative(getCardinal(), 2).getRelative(MathUtil.anticlockwise(getCardinal())));

			// InputRegistry[0] = start/stop command
			this.addInputRegistry(new PinRegistry<InputPin>(wire));

			triggerBC7003();

			if (! ByteCart.myPlugin.getUm().isUpdater(this.getVehicle().getEntityId())) {

				// if this is a cart in a train
				if (this.wasTrain(this.getLocation())) {
					ByteCart.myPlugin.getIsTrainManager().getMap().reset(getLocation());
					//				this.getOutput(0).setAmount(3);	// push buttons
					return;
				}

				// if this is the first car of a train
				// we keep the state during 2 s
				if (this.isTrain()) {
					this.setWasTrain(this.getLocation(), true);
				}

				this.route();

				return;
			}

			// it's an updater, so let it choosing direction
			Updater updater = UpdaterFactory.getUpdater(this);

			// routing
			this.getOutput(0).setAmount(0); // unpower levers

			// here we perform routes update
			updater.doAction(Side.LEFT);
		}
		catch (ClassCastException e) {
			if(ByteCart.debug)
				ByteCart.log.info("ByteCart : " + e.toString());

			// Not the good blocks to build the signs
			return;
		}
		catch (NullPointerException e) {
			if(ByteCart.debug)
				ByteCart.log.info("ByteCart : "+ e.toString());
			e.printStackTrace();

			// there was no inventory in the cart
			return;
		}


	}

	public void power() {
		this.powerBC7003();
	}

	protected void triggerBC7003() {
		(new BC7003(this.getBlock())).trigger();
	}

	protected void powerBC7003() {
		(new BC7003(this.getBlock())).power();
	}

	private boolean in(int l, int v, int h) {
		return l <= v && v <= h;
	}

	protected boolean isAddressInRange() {
		/*
		int startRegion = this.getInput(INPUT_RANGE_START_REGION).getAmount();
		int endRegion = this.getInput(INPUT_RANGE_END_REGION).getAmount();
		int startTrack = this.getInput(INPUT_RANGE_START_TRACK).getAmount();
		int endTrack = this.getInput(INPUT_RANGE_END_TRACK).getAmount();
		int startStation = this.getInput(INPUT_RANGE_START_STATION).getAmount();
		int endStation = this.getInput(INPUT_RANGE_END_STATION).getAmount();

		int region = this.getInput(INPUT_IP_REGION).getAmount();
		int track = this.getInput(INPUT_IP_TRACK).getAmount();
		int station = this.getInput(INPUT_IP_STATION).getAmount();
		*/

		Address rangeStart = getRangeStartAddress();
		Address rangeEnd = getRangeEndAddress();
		Address ip = getVehicleAddress();

		int startRegion = rangeStart.getRegion().getAmount();
		int region = ip.getRegion().getAmount();
		int endRegion = rangeEnd.getRegion().getAmount();

		int startTrack = rangeStart.getTrack().getAmount();
		int track = ip.getTrack().getAmount();
		int endTrack = rangeEnd.getTrack().getAmount();

		int startStation = rangeStart.getStation().getAmount();
		int station = ip.getStation().getAmount();
		int endStation = rangeEnd.getStation().getAmount();

		boolean value =	in(startRegion, region, endRegion) &&
				in(startTrack, track, endTrack) &&
				in(startStation, station, endStation);

		if(negated())
			return !value;
		return value;
	}

	protected boolean isCollisionInputOk() {
		return this.getInput(6).getAmount() == 0;
	}

	protected boolean negated() {
		return false;
	}

	protected SimpleCollisionAvoider.Side route() {
		SignPreStationEvent event;
		SignPostStationEvent event1;

		// test if every destination field matches sign field
		if (this.isAddressInRange() && this.isCollisionInputOk())
			event = new SignPreStationEvent(this, Side.RIGHT); // power levers if matching
		else
			event = new SignPreStationEvent(this, Side.LEFT); // unpower levers if not matching
		Bukkit.getServer().getPluginManager().callEvent(event);

		// TODO: WTF is this doing?
		if (event.getSide().equals(Side.RIGHT) && this.isCollisionInputOk()) {
			this.getOutput(0).setAmount(3); // power levers if matching
			event1 = new SignPostStationEvent(this, Side.RIGHT);
		} else {
			this.getOutput(0).setAmount(0); // unpower levers if not matching
			event1 = new SignPostStationEvent(this, Side.RIGHT);
		}
		Bukkit.getServer().getPluginManager().callEvent(event1);

		return null;
	}

	@Override
	public String getName() {
		return "BC9037";
	}

	@Override
	public String getFriendlyName() {
		return "Range matcher";
	}

}
