package com.myappconverter.sprite4droid;

import org.cocos2dx.lib.Sprite4DroidActivity;
import android.os.Bundle;
import com.myappconverter.java.coregraphics.CGSize;
import com.myappconverter.java.spritekit.SKView;


//This is the main Activity of our game
public class MainActivity extends Sprite4DroidActivity {
	/** Called when the activity is first created. */
	
	
	//a basic onCreate Method
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	
	//The main method of our Sprite4Droid Activities
	public void initWithSize(CGSize size) {
		MainScene scene = MainScene.sceneWithSize(MainScene.class, size); // we create our Scene 
		SKView.getInstance().setShowsFPS(false); // we can set some SKView properties
		SKView.getInstance().presentScene(scene); // we push our MainScene 
		
	}
	
	
}