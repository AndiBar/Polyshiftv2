package hamburg.haw.polyshift.Menu;

public class SaveValue {
	
	private static String selectedFriendName;
	
	public static void setSelectedFriendName (String newName) {
		selectedFriendName = newName;
	}
	
	public static String getSelectedFriendName() {
		return selectedFriendName;
	}

}
