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
	public static class collectionView{};
	public static class settingView{};
	public static class quesSetView{};
	public static class quesAgendaView{};
	public static class scoreView{};
	public static class sessionView{};
	public static class feedScheduleView{};
	public static class feedDeliveryView{};
	public static class assesmentView{};
	public static class deliveredFeedsView{};
}
