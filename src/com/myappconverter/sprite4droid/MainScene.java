package com.myappconverter.sprite4droid;

import com.myappconverter.java.avfoundation.AVAudioPlayer;
import com.myappconverter.java.coregraphics.CGPoint;
import com.myappconverter.java.coregraphics.CGSize;
import com.myappconverter.java.coregraphics.CGVector;
import com.myappconverter.java.foundations.NSArray;
import com.myappconverter.java.foundations.NSBundle;
import com.myappconverter.java.foundations.NSError;
import com.myappconverter.java.foundations.NSSet;
import com.myappconverter.java.foundations.NSString;
import com.myappconverter.java.foundations.NSURL;
import com.myappconverter.java.ios.include.Math;
import com.myappconverter.java.spritekit.SKAction;
import com.myappconverter.java.spritekit.SKAction.SKActionBlock.runBlockBlock;
import com.myappconverter.java.spritekit.SKNode;
import com.myappconverter.java.spritekit.SKPhysicsBody;
import com.myappconverter.java.spritekit.SKPhysicsContact;
import com.myappconverter.java.spritekit.SKPhysicsContactDelegate;
import com.myappconverter.java.spritekit.SKScene;
import com.myappconverter.java.spritekit.SKSpriteNode;
import com.myappconverter.java.spritekit.SKTransition;
import com.myappconverter.java.uikit.UIColor;
import com.myappconverter.java.uikit.UIEvent;
import com.myappconverter.java.uikit.UITouch;

public class MainScene extends SKScene implements SKPhysicsContactDelegate{
	
	//Some properties
	SKSpriteNode player; // our player
	//Collision masks
	static long projectileCategory = 0x1 << 0;
	static long monsterCategory = 0x1 << 1;
	//Music Player
	AVAudioPlayer backgroundMusicPlayer;
	//some other properties
	int monstersDestroyed;
	double lastSpawnTimeInterval;
	double lastUpdateTimeInterval;
	
	
	@Override
	public SKScene initWithSize(CGSize _size) {
		super.initWithSize(_size);
		
		//PhysicsWorld properties
		this.getPhysicsWorld().setGravity(new CGVector(0, 0));
		this.getPhysicsWorld().setContactDelegate(this);
		
		//The background color
		this.setBackgroundColor(UIColor.colorWithRedGreenBlueAlpha(1.0f, 1.0f, 1.0f, 1.0f)); // this is white color
		
		//Setting our Player
		this.player = SKSpriteNode.spriteNodeWithImageNamed(SKSpriteNode.class, new NSString("player"));
		this.player.setPosition(CGPoint.make(100, _size.height / 2));
		this.addChild(this.player);
		
		//The background Music
		NSError error = null;
		NSURL backgroundMusicURL = NSBundle.mainBundle().URLForResourceWithExtension(
				new NSString("background-music-aac"), new NSString("mp3"));
		this.backgroundMusicPlayer = new AVAudioPlayer();
		this.backgroundMusicPlayer.initWithContentsOfURLError(backgroundMusicURL, error);
		this.backgroundMusicPlayer.setNumberOfLoops(-1);
		this.backgroundMusicPlayer.prepareToPlay();
		this.backgroundMusicPlayer.play();
		return this;
	}
	
	//The main update method
	@Override
	public void update(double currentTime) {
		super.update(currentTime);
		currentTime = System.currentTimeMillis();
		double timeSinceLast = currentTime - this.lastUpdateTimeInterval;
		this.lastUpdateTimeInterval = currentTime;
		if (timeSinceLast > 1) { // more than a second since last update
			timeSinceLast = 1.0f / 60.0f;
			this.lastUpdateTimeInterval = currentTime;
		}
		this.updateWithTimeSinceLastUpdate(timeSinceLast);
	}
	
	
	//Methods for collision detection 
	@Override
	public void didBeginContact(SKPhysicsContact contact) {

		// 1
		SKPhysicsBody firstBody, secondBody;

		if (contact.getBodyA().getCategoryBitMask() < contact.getBodyB().getCategoryBitMask()) {
			firstBody = contact.getBodyA();
			secondBody = contact.getBodyB();
		} else {
			firstBody = contact.getBodyB();
			secondBody = contact.getBodyA();
		}

		// 2
		if ((firstBody.getCategoryBitMask() & projectileCategory) != 0
				&& (secondBody.getCategoryBitMask() & monsterCategory) != 0) {
			this.projectileDidCollideWithMonster(firstBody.getNode(), secondBody.getNode());
		}
	}

	@Override
	public void didEndContact(SKPhysicsContact contact) {
		
	}
	
	
	
	//Touch handling
	@Override
	public void touchesBeganWithEvent(NSSet<UITouch> touches, UIEvent event) {

		super.touchesBeganWithEvent(touches, event);
		// 1 - Choose one of the touches to work with
		UITouch touch = (UITouch) touches.anyObject();
		CGPoint location = touch.locationInNode(this);

		// 2 - Set up initial location of projectile
		SKSpriteNode projectile = SKSpriteNode.spriteNodeWithImageNamed(SKSpriteNode.class, new NSString("projectile"));
		projectile.setPosition(this.player.getPosition());
		projectile.setPhysicsBody(SKPhysicsBody.bodyWithCircleOfRadius(projectile.getSize().width / 2));
		projectile.getPhysicsBody().setDynamic(true);
		projectile.getPhysicsBody().setCategoryBitMask(projectileCategory);
		projectile.getPhysicsBody().setContactTestBitMask(monsterCategory);
		projectile.getPhysicsBody().setCollisionBitMask(0);
		projectile.getPhysicsBody().setUsesPreciseCollisionDetection(true);

		// 3- Determine offset of location to projectile
		CGPoint offset = rwSub(location, projectile.getPosition());

		// 4 - Bail out if you are shooting down or backwards
		if (offset.x <= 0)
			return;

		// 5 - OK to add now - we've double checked position
		this.addChild(projectile);

		// 6 - Get the direction of where to shoot
		CGPoint direction = rwNormalize(offset);

		// 7 - Make it shoot far enough to be guaranteed off screen
		CGPoint shootAmount = rwMult(direction, 1000);

		// 8 - Add the shoot amount to the current position
		CGPoint realDest = rwAdd(shootAmount, projectile.getPosition());

		// 9 - Create the actions
		float velocity = (float) (480.0 / 1.0);
		float realMoveDuration = this.getSize().width / velocity;
		SKAction actionMove = SKAction.moveToDuration(realDest, realMoveDuration);
		SKAction actionMoveDone = SKAction.removeFromParent();
		projectile.runAction(SKAction.sequence(NSArray.arrayWithObjects(actionMove, actionMoveDone)));
	}
	
	@Override
	public void touchesEndedWithEvent(NSSet<UITouch> touches, UIEvent event) {

		super.touchesEndedWithEvent(touches, event);
		this.runAction(SKAction.playSoundFileNamedWaitForCompletion(new NSString("pewpew.wav"), false));
	}
	
	
	//Some other functions 
	public void updateWithTimeSinceLastUpdate(double timeSinceLast) {

		this.lastSpawnTimeInterval += timeSinceLast;
		if (this.lastSpawnTimeInterval > 1) {
			this.lastSpawnTimeInterval = 0;
			this.addMonster();
		}
	}
	
	public void addMonster() {
		SKSpriteNode monster = SKSpriteNode.spriteNodeWithImageNamed(SKSpriteNode.class, new NSString("monster"));
		monster.setPhysicsBody(SKPhysicsBody.bodyWithRectangleOfSize(monster.getSize())); 
		monster.getPhysicsBody().setDynamic(true); 
		monster.getPhysicsBody().setCategoryBitMask(monsterCategory); 
		monster.getPhysicsBody().setContactTestBitMask(projectileCategory); 
		monster.getPhysicsBody().setCollisionBitMask(0); 

		// Determine where to spawn the monster along the Y axis
		int minY = (int) (monster.getSize().height / 2);
		int maxY = (int) (this.getFrame().size.height - monster.getSize().height / 2);
		int rangeY = maxY - minY;
		int actualY = (Math.arc4random() % rangeY) + minY;

		// Create the monster slightly off-screen along the right edge,
		// and along a random position along the Y axis as calculated above
		monster.setPosition(CGPoint.make(this.getFrame().size.width + monster.getSize().width / 2, actualY));
		this.addChild(monster);

		// Determine speed of the monster
		int minDuration = (int) 2.0;
		int maxDuration = (int) 4.0;
		int rangeDuration = maxDuration - minDuration;
		int actualDuration = (Math.arc4random() % rangeDuration) + minDuration;

		// Create the actions
		SKAction actionMove = SKAction.moveToDuration(CGPoint.make(-monster.getSize().width / 2, actualY),
				actualDuration);
		SKAction actionMoveDone = SKAction.removeFromParent();
		// monster.runAction(SKAction.sequence(NSArray.arrayWithObjects(actionMove, actionMoveDone)));
		SKAction loseAction = SKAction.runBlock(new runBlockBlock() {

			@Override
			public void perform() {
				// TODO Auto-generated method stub
				SKTransition reveal = SKTransition.flipHorizontalWithDuration(0.5);
				GameOverScene gameOverScene = GameOverScene.sceneWithSize(GameOverScene.class, MainScene.this.getSize());
				gameOverScene.initWithSizeWon(MainScene.this.getSize(), false);
				MainScene.this.getView().presentSceneTransition(gameOverScene, reveal);
			}
		});
		monster.runAction(SKAction.sequence(NSArray.arrayWithObjects(actionMove, loseAction, actionMoveDone)));
	}
	
	public static CGPoint rwAdd(CGPoint a, CGPoint b) {
		return CGPoint.make(a.x + b.x, a.y + b.y);
	}

	static CGPoint rwSub(CGPoint a, CGPoint b) {
		return CGPoint.make(a.x - b.x, a.y - b.y);
	}

	static CGPoint rwMult(CGPoint a, float b) {
		return CGPoint.make(a.x * b, a.y * b);
	}

	static float rwLength(CGPoint a) {
		return Math.sqrtf(a.x * a.x + a.y * a.y);
	}

	static CGPoint rwNormalize(CGPoint a) {
		float length = rwLength(a);
		return CGPoint.make(a.x / length, a.y / length);
	}

	public void projectileDidCollideWithMonster(SKNode projectile, SKNode monster) {
		
		projectile.removeFromParent();
		monster.removeFromParent();

		this.monstersDestroyed++;
		if (this.monstersDestroyed > 10) {
			SKTransition reveal = SKTransition.flipHorizontalWithDuration(0.5);
			GameOverScene gameOverScene = GameOverScene.sceneWithSize(GameOverScene.class, MainScene.this.getSize());
			gameOverScene.initWithSizeWon(MainScene.this.getSize(), true);
			MainScene.this.getView().presentSceneTransition(gameOverScene, reveal);
		}
	}
	
	
}
