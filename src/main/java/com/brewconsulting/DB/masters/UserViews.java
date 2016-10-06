package com.brewconsulting.DB.masters;

public class UserViews {
	public static class bareView{}
	public static class authView extends bareView{};
	public static class profileView extends authView{};
	public static class deAssociateView{};
}
