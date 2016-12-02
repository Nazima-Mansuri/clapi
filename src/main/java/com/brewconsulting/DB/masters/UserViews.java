package com.brewconsulting.DB.masters;

public class UserViews {
	public static class bareView{}
	public static class authView extends bareView{};
	public static class profileView extends authView{};
	public static class deAssociateView{};
	public static class groupTaskView{};
	public static class childTaskView{};
	public static class groupNoteView{};
	public static class childNoteView{};
	public static class clientView{};
	public static class divView{};
}
