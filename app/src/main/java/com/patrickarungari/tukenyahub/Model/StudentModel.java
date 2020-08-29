package com.patrickarungari.tukenyahub.Model;

import com.google.gson.annotations.SerializedName;

public class StudentModel{

	@SerializedName("uniNum")
	private String uniNum;

	@SerializedName("imagePath")
	private String imagePath;

	@SerializedName("id")
	private int id;

	@SerializedName("error")
	private boolean error;

	@SerializedName("email")
	private String email;

	@SerializedName("username")
	private String username;

	public void setUniNum(String uniNum){
		this.uniNum = uniNum;
	}

	public String getUniNum(){
		return uniNum;
	}

	public void setImagePath(String imagePath){
		this.imagePath = imagePath;
	}

	public String getImagePath(){
		return imagePath;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setError(boolean error){
		this.error = error;
	}

	public boolean isError(){
		return error;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getUsername(){
		return username;
	}
}