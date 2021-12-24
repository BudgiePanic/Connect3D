package connect3DCore;
/**
 * A direction of travel through the board.
 * There are 26 directions you can travel in, from a given point in the 3D space.
 * These 26 directions have been captured as the following constants below plus and identity that represents no direction.
 * @author Benjamin
 *
 */
@FunctionalInterface
interface Direction {
	/**
	 * Given a location, what is the next location along a given direction.
	 * @param input
	 *  The starting location.
	 * @return
	 *  The next location in this direction.
	 */
	Tuple next(Tuple input);
	
	Direction IDENTITY = (t) -> new Tuple(t.x, t.y, t.z);
	
	Direction UP = (t) -> new Tuple(t.x, t.y + 1, t.z);
	Direction DOWN = (t) -> new Tuple(t.x, t.y - 1, t.z);
	
	Direction LEFT = (t) -> new Tuple(t.x - 1, t.y, t.z);
	Direction RIGHT = (t) -> new Tuple(t.x + 1, t.y, t.z);
	Direction TOWARDS = (t) -> new Tuple(t.x, t.y, t.z - 1);
	Direction AWAY = (t) -> new Tuple(t.x, t.y, t.z + 1);
	
	//Flat diagonals
	Direction LeftANDtowards = (t) -> new Tuple(t.x - 1, t.y, t.z - 1);
	Direction LeftANDaway = (t) -> new Tuple(t.x - 1, t.y, t.z + 1);
	Direction RightANDaway = (t) -> new Tuple(t.x + 1, t.y, t.z + 1);
	Direction RightANDtowards = (t) -> new Tuple(t.x + 1, t.y, t.z - 1);
	
	//Left AND right Height diagonals
	Direction RightANDup = (t) -> new Tuple(t.x + 1, t.y + 1, t.z);
	Direction LeftANDup = (t) -> new Tuple(t.x - 1, t.y + 1, t.z);
	Direction RightANDdown = (t) -> new Tuple(t.x + 1, t.y - 1, t.z);
	Direction LeftANDdown = (t) -> new Tuple(t.x - 1, t.y - 1, t.z);
	
	//Towards AND away Height diagonals
	Direction TowardsANDdown = (t) -> new Tuple(t.x, t.y - 1, t.z - 1);
	Direction TowardsANDup = (t) -> new Tuple(t.x, t.y + 1, t.z - 1);
	Direction AwayANDdown = (t) -> new Tuple(t.x, t.y - 1, t.z + 1);
	Direction AwayANDup = (t) -> new Tuple(t.x, t.y + 1, t.z + 1);
	
	//Towards AND Left AND Height
	Direction LeftANDtowardsANDdown = (t) -> new Tuple(t.x - 1, t.y - 1, t.z - 1);
	Direction LeftANDtowardsANDup = (t) -> new Tuple(t.x - 1, t.y + 1, t.z - 1);
	
	//Towards AND Right AND height
	Direction RightANDtowardsANDup = (t) -> new Tuple(t.x + 1, t.y + 1, t.z - 1);
	Direction RightANDtowardsANDdown = (t) -> new Tuple(t.x + 1, t.y - 1, t.z - 1);
	
	//Away AND Left AND Height
	Direction LeftANDawayANDup = (t) -> new Tuple(t.x - 1, t.y + 1, t.z + 1);
	Direction LeftANDawayANDdown = (t) -> new Tuple(t.x - 1, t.y - 1, t.z + 1);
	
	//Away AND Right AND Height
	Direction RightANDawayANDup = (t) -> new Tuple(t.x + 1, t.y + 1, t.z + 1);
	Direction RightANDawayANDdown = (t) -> new Tuple(t.x + 1, t.y - 1, t.z + 1);
}
