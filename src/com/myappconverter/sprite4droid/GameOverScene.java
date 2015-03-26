package com.myappconverter.sprite4droid;

import com.myappconverter.java.coregraphics.CGPoint;
import com.myappconverter.java.coregraphics.CGSize;
import com.myappconverter.java.foundations.NSArray;
import com.myappconverter.java.foundations.NSString;
import com.myappconverter.java.spritekit.SKAction;
import com.myappconverter.java.spritekit.SKAction.SKActionBlock.runBlockBlock;
import com.myappconverter.java.spritekit.SKLabelNode;
import com.myappconverter.java.spritekit.SKScene;
import com.myappconverter.java.spritekit.SKTransition;
import com.myappconverter.java.uikit.UIColor;

//We will also need a GameOverScene to show the game over message
public class GameOverScene extends SKScene {

	
	//we need just an init method 
	public SKScene initWithSizeWon(CGSize _size, boolean won) {

		if (super.initWithSize(_size) != null) {

			// 1
			this.setBackgroundColor(UIColor.colorWithRedGreenBlueAlpha(1.0f, 1.0f, 1.0f, 1.0f));

			// 2
			NSString message;
			if (won) {
				message = new NSString("You Won!");
			} else {
				message = new NSString("You Lose :[");
			}

			// 3
			SKLabelNode label = SKLabelNode.labelNodeWithFontNamed(SKLabelNode.class, new NSString("Chalkduster"));
			label.setText(message);
			label.setFontSize(40);
			label.setFontColor(UIColor.blackColor());
			label.setPosition(CGPoint.make(this.getSize().width / 2, this.getSize().height / 2));
			this.addChild(label);

			this.runAction(SKAction.sequence(NSArray.arrayWithObjects(SKAction.waitForDuration(3.0),
					SKAction.runBlock(new runBlockBlock() {

						@Override
						public void perform() {
							SKTransition reveal = SKTransition.flipHorizontalWithDuration(0.5);
							SKScene mainScene = MainScene.sceneWithSize(MainScene.class, GameOverScene.this.getSize());
							GameOverScene.this.getView().presentSceneTransition(mainScene, reveal);

						}
					}))));
		}
		return this;

	}
}