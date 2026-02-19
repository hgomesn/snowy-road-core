package com.app;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

// Class
public class Main extends ApplicationAdapter {
	// const
	static final float SCREEN_WIDTH = 1920; // default screen width
	static final float SCREEN_HEIGHT = 1440; // default screen height
	static final float PPM = 32; // pixels per meter in Box2D world
	final boolean SHOW_DEBUG = false; // show debug
	final float CAMERA_OFFSET_X = 400; // camera offset left
	final float CAMERA_OFFSET_Y = 0; // camera offset top
	final float BRIGHTNESS_PRESSED = 0.9f; // button brightness when pressed
	final float ACCELERATION = 5f; // hero acceleration
	final float MAX_SPEED = 20f; // hero max speed
	final float PART_WIDTH = 4032; // map part width
	final int TIME = 20; // full time
	static final int MIN_TIME = 10; // min time when show gas

	// vars
	static Stage stage;
	static World world;
	static AssetManager assetManager;
	static InputListener controlListener;
	static float ratio;
	static Array<Body> destroyBodies;
	static Array<Joint> destroyJoints;
	OrthographicCamera cam;
	JsonValue map;
	float mapWidth;
	float mapHeight;
	boolean keyLeft;
	boolean keyRight;
	boolean isSigned;
	Preferences pref;
	Box2DDebugRenderer debug;
	String screenColor;
	SpriteBatch batch;
	int currentWidth;
	int currentHeight;
	Viewport viewport;
	InterfaceListener nativePlatform;
	float taskDelay;
	Music sndBg;
	boolean gamePaused;
	Act controlLeft;
	Act controlRight;
	Act btnSignIn;
	Act btnSignOut;
	Act btnSound;
	Act btnMute;
	Act btnPause;
	Group groupPause;
	int score;
	Task TIMER;
	String screen = ""; // screen

	Joint jointTire0;
	Joint jointBag;
	Joint jointHeroLive;
	Act hero;
	Act car;
	Act bag;
	Act tire0;
	Act tire1;
	Act progressOver;
	Act progressLine;
	Act progressBg;
	Group groupGameOver;
	Group groupTimeUp;
	int numPart;
	Array<Act> startActors;
	Array<Act> screenActors;
	Array<Act> leftActors;
	float leftLimit;
	static Array<AtlasRegion> gifts;
	TextureAtlas numbers;
	static int currentTime;
	Sound sndSnowman;
	Sound sndGameOver;
	Sound sndSpring;
	ParticleEffect effectSnow;

	// Constructor
	public Main(InterfaceListener nativePlatform) {
		this.nativePlatform = nativePlatform;
	}

	@Override
	public void create() {
		batch = new SpriteBatch();
		controlListener = new CONTROL();
		destroyBodies = new Array<Body>();
		destroyJoints = new Array<Joint>();
		currentWidth = Gdx.graphics.getWidth();
		currentHeight = Gdx.graphics.getHeight();
		assetManager = new AssetManager();
		Gdx.input.setCatchKey(Keys.BACK, true); // prevent back on mobile
		startActors = new Array<Act>();
		screenActors = new Array<Act>();
		leftActors = new Array<Act>();
		TIMER();

		// load assets
		Lib.loadAssets(false);

		// debug
		if (SHOW_DEBUG)
			debug = new Box2DDebugRenderer();

		// preferences
		pref = Gdx.app.getPreferences("preferences");

		// send score
		if (pref.contains("score"))
			nativePlatform.saveScore(pref.getInteger("score"));

		// camera & viewport
		cam = new OrthographicCamera(SCREEN_WIDTH / PPM, SCREEN_HEIGHT / PPM);
		viewport = new FillViewport(SCREEN_WIDTH, SCREEN_HEIGHT);

		// world
		world = new World(new Vector2(0, -10f), true);
		world.setContactListener(new CONTACT());

		// stage
		stage = new Stage(viewport, batch);
		Gdx.input.setInputProcessor(stage);

		// sound bg
		sndBg = assetManager.get("sndBg.mp3", Music.class);
		bgSound();

		// effectSnow
		effectSnow = new ParticleEffect();
		effectSnow.load(Gdx.files.internal("snowEffect"), Gdx.files.internal(""));
		effectSnow.setPosition(0, SCREEN_HEIGHT);

		// atlases
		gifts = assetManager.get("gift.atlas", TextureAtlas.class).getRegions();
		numbers = assetManager.get("number.atlas", TextureAtlas.class);

		showScreen("main");
	}
    	// showScreen
	void showScreen(String screen) {
		clearScreen();
		this.screen = screen;

		if (screen.equals("main")) { // MAIN
			// load screen
			map = new JsonReader().parse(Gdx.files.internal(screen + ".hmp"));
			Lib.addLayers(null, map, stage.getRoot());

			// sign buttons
			btnSignIn = Lib.getActors("btnSignIn", stage.getRoot()).first();
			btnSignOut = Lib.getActors("btnSignOut", stage.getRoot()).first();
			setSigned(isSigned);

			// sound buttons
			btnSound = Lib.getActors("btnSound", stage.getRoot()).first();
			btnMute = Lib.getActors("btnMute", stage.getRoot()).first();
			if (pref.getBoolean("mute", false)) {
				btnMute.setVisible(false);
				btnSound.setVisible(true);
			} else {
				btnSound.setVisible(false);
				btnMute.setVisible(true);
			}

			// map config
			mapWidth = map.getInt("map_width", 0);
		} else if (screen.equals("game")) { // GAME
			// load screen
			map = new JsonReader().parse(Gdx.files.internal("game.hmp"));

			// sounds
			sndSnowman = assetManager.get("sndSnowman.mp3", Sound.class);
			sndGameOver = assetManager.get("sndGameOver.mp3", Sound.class);
			sndSpring = assetManager.get("sndSpring.mp3", Sound.class);

			// start part
			startActors = Lib.addLayers("bg", map, stage.getRoot());
			startActors.addAll(Lib.addLayers("start", map, stage.getRoot()));

			mapWidth = PART_WIDTH;
			score = 0;
			currentTime = TIME;
			Timer.schedule(TIMER, 1, 1);

			// hero
			bag = Lib.addLayers("bag", map, stage.getRoot()).first();
			hero = Lib.addLayers("hero", map, stage.getRoot()).first();
			car = Lib.addLayers("car", map, stage.getRoot()).first();
			tire1 = Lib.addLayers("tire1", map, stage.getRoot()).first();
			tire0 = Lib.addLayers("tire0", map, stage.getRoot()).first();

			// progress
			progressBg = Lib.addLayers("progressBg", map, stage.getRoot()).first();
			progressLine = Lib.addLayers("progressLine", map, stage.getRoot()).first();
			progressOver = Lib.addLayers("progressOver", map, stage.getRoot()).first();

			// jointTire0
			WheelJointDef joint = new WheelJointDef();
			joint.bodyA = car.body;
			joint.bodyB = tire0.body;
			joint.localAnchorA.set(-76 / PPM, -42 / PPM);
			joint.localAnchorB.set(0, 0);
			joint.localAxisA.set(0.1f, 0.8f);
			joint.frequencyHz = 4;
			jointTire0 = world.createJoint(joint);

			// jointTire1
			joint.bodyA = car.body;
			joint.bodyB = tire1.body;
			joint.localAnchorA.set(76 / PPM, -42 / PPM);
			joint.localAnchorB.set(0, 0);
			joint.localAxisA.set(-0.1f, 0.8f);
			joint.frequencyHz = 4;
			world.createJoint(joint);

			// jointBag
			joint.bodyA = car.body;
			joint.bodyB = bag.body;
			joint.localAnchorA.set(-77 / PPM, -1 / PPM);
			joint.localAnchorB.set(-35 / PPM, -10 / PPM);
			joint.localAxisA.set(0.5f, 0.5f);
			joint.frequencyHz = 5;
			joint.collideConnected = true;
			jointBag = world.createJoint(joint);

			// jointHeroLive
			joint.bodyA = car.body;
			joint.bodyB = hero.body;
			joint.localAnchorA.set(-6 / PPM, 33 / PPM);
			joint.localAnchorB.set(-20 / PPM, 5 / PPM);
			joint.localAxisA.set(0.2f, 0.2f);
			joint.collideConnected = true;
			jointHeroLive = world.createJoint(joint);

			// jointHero
			RevoluteJointDef joint2 = new RevoluteJointDef();
			joint2.bodyA = car.body;
			joint2.bodyB = hero.body;
			joint2.localAnchorA.set(-11 / PPM, -15 / PPM);
			joint2.localAnchorB.set(-23 / PPM, -44 / PPM);
			joint2.collideConnected = true;
			joint2.lowerAngle = (float) Math.toRadians(-60);
			joint2.upperAngle = (float) Math.toRadians(0);
			joint2.enableLimit = true;
			world.createJoint(joint2);

			// controls
			controlLeft = Lib.addLayers("controlLeft", map, stage.getRoot()).first();
			controlRight = Lib.addLayers("controlRight", map, stage.getRoot()).first();
			btnPause = Lib.addLayers("btnPause", map, stage.getRoot()).first();

			// group
			groupGameOver = Lib.addGroup("groupGameOver", map, stage.getRoot());
			groupTimeUp = Lib.addGroup("groupTimeUp", map, stage.getRoot());

			// groupPause
			groupPause = Lib.addGroup("groupPause", map, stage.getRoot());
			btnSound = groupPause.findActor("btnSoundPause");
			btnMute = groupPause.findActor("btnMutePause");
			if (pref.getBoolean("mute", false)) {
				btnMute.setVisible(false);
				btnSound.setVisible(true);
			} else {
				btnSound.setVisible(false);
				btnMute.setVisible(true);
			}

			// controls animation
			controlLeft.addAction(Actions.sequence(Actions.delay(3), Actions.alpha(0, 1)));
			controlRight.addAction(Actions.sequence(Actions.delay(3), Actions.alpha(0, 1)));

			// sound
			if (!pref.getBoolean("mute", false))
				assetManager.get("sndGo.mp3", Sound.class).play(0.3f);
		}

		// map config
		mapHeight = map.getInt("map_height", 0);
		screenColor = map.getString("map_color", null);

		// stage keyboard focus
		Act a = new Act("");
		stage.addActor(a);
		a.addListener(controlListener);
		stage.setKeyboardFocus(a);

		render();
	}

	// clearScreen
	void clearScreen() {
		screen = "";
		screenColor = null;
		gamePaused = false;
		keyLeft = false;
		keyRight = false;
		score = 0;
		jointBag = null;
		jointTire0 = null;
		jointHeroLive = null;
		numPart = 0;
		leftLimit = 0;
		startActors.clear();
		screenActors.clear();
		leftActors.clear();
		TIMER.cancel();
		if (sndGameOver != null)
			sndGameOver.stop();

		// clear world
		if (world != null) {
			world.clearForces();
			world.getJoints(destroyJoints);
			world.getBodies(destroyBodies);
		}
		render();

		// clear stage
		stage.clear();
	}

	@Override
	public void render() {
		// screen color
		if (screenColor != null) {
			Color color = Color.valueOf(screenColor);
			Gdx.gl.glClearColor(color.r, color.g, color.b, 1);
		}

		// clear
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// current screen render
		if (screen.equals("game"))
			renderGame();
		else if (!screen.isEmpty()) {
			// camera position
			stage.getRoot().setPosition((SCREEN_WIDTH - mapWidth) * 0.5f, (SCREEN_HEIGHT - mapHeight) * 0.5f);
			cam.position.set((SCREEN_WIDTH * 0.5f - stage.getRoot().getX()) / PPM,
					(SCREEN_HEIGHT * 0.5f - stage.getRoot().getY()) / PPM, 0);
			cam.update();

			// world render
			world.step(1 / 30f, 8, 3);

			// stage render
			stage.act(Gdx.graphics.getDeltaTime());
			stage.draw();

			// effectSnow
			batch.begin();
			effectSnow.draw(batch, 0.015f);
			batch.end();
		}

		// destroy
		if (!world.isLocked()) {
			for (int i = 0; i < destroyJoints.size; i++)
				world.destroyJoint(destroyJoints.get(i));
			for (int i = 0; i < destroyBodies.size; i++)
				world.destroyBody(destroyBodies.get(i));
			destroyJoints.clear();
			destroyBodies.clear();
		}

		// debug render
		if (SHOW_DEBUG)
			debug.render(world, cam.combined);
	}

	// renderGame
	void renderGame() {
		if (!gamePaused) {
			Vector2 point = null;

			// hero
			if (hero.enabled) {
				if (currentTime == 0 || (!keyLeft && !keyRight)) {
					tire0.body.setAngularVelocity(tire1.body.getAngularVelocity() * 0.95f);
					tire1.body.setAngularVelocity(tire1.body.getAngularVelocity() * 0.95f);
				} else if (keyLeft) {
					tire0.body.setAngularVelocity(Math.min(tire0.body.getAngularVelocity() + ACCELERATION, MAX_SPEED));
					tire1.body.setAngularVelocity(Math.min(tire1.body.getAngularVelocity() + ACCELERATION, MAX_SPEED));
				} else if (keyRight) {
					tire0.body.setAngularVelocity(Math.max(tire0.body.getAngularVelocity() - ACCELERATION, -MAX_SPEED));
					tire1.body.setAngularVelocity(Math.max(tire1.body.getAngularVelocity() - ACCELERATION, -MAX_SPEED));
				}
			} else
				tire1.body.setAngularVelocity(tire1.body.getAngularVelocity() * 0.98f);

			// camera position
			if (mapWidth < SCREEN_WIDTH)
				stage.getRoot().setX((SCREEN_WIDTH - mapWidth) * 0.5f);
			else
				stage.getRoot().setX(
						MathUtils.clamp(SCREEN_WIDTH * 0.5f - CAMERA_OFFSET_X - hero.getX(),
								SCREEN_WIDTH - mapWidth + viewport.getLeftGutterWidth() / ratio, -viewport.getLeftGutterWidth()
										/ ratio - leftLimit));
			if (mapHeight < SCREEN_HEIGHT)
				stage.getRoot().setY((SCREEN_HEIGHT - mapHeight) * 0.5f);
			else
				stage.getRoot().setY(
						MathUtils.clamp(SCREEN_HEIGHT * 0.5f - CAMERA_OFFSET_Y - hero.getY(), SCREEN_HEIGHT - mapHeight
								+ viewport.getTopGutterHeight() / ratio, -viewport.getTopGutterHeight() / ratio));
			cam.position.set((SCREEN_WIDTH * 0.5f - stage.getRoot().getX()) / PPM,
					(SCREEN_HEIGHT * 0.5f - stage.getRoot().getY()) / PPM, 0);
			cam.update();

			// groups
			groupPause.setPosition(-stage.getRoot().getX(), -stage.getRoot().getY());
			groupGameOver.setPosition(-stage.getRoot().getX(), -stage.getRoot().getY());
			groupTimeUp.setPosition(-stage.getRoot().getX(), -stage.getRoot().getY());

			// controlLeft
			point = stage.screenToStageCoordinates(new Vector2(0, Gdx.graphics.getHeight()));
			controlLeft.setPosition(point.x - stage.getRoot().getX(), point.y - stage.getRoot().getY());

			// controlRight
			point = stage.screenToStageCoordinates(new Vector2(Gdx.graphics.getWidth() - controlRight.getWidth() * ratio,
					Gdx.graphics.getHeight()));
			controlRight.setPosition(point.x - stage.getRoot().getX(), point.y - stage.getRoot().getY());

			// btnPause
			point = stage.screenToStageCoordinates(new Vector2(Gdx.graphics.getWidth() - (btnPause.getWidth() + 20) * ratio,
					(btnPause.getHeight() + 20) * ratio));
			btnPause.setPosition(point.x - stage.getRoot().getX(), point.y - stage.getRoot().getY());

			// render
			world.step(1 / 30f, 8, 3);
			stage.act(Gdx.graphics.getDeltaTime());

			// progress
			point = stage.screenToStageCoordinates(new Vector2(10 * ratio, (progressBg.getHeight() + 10) * ratio));
			progressBg.setPosition(point.x - stage.getRoot().getX(), point.y - stage.getRoot().getY());
			progressOver.setPosition(progressBg.getX(), progressBg.getY());
			point = stage.screenToStageCoordinates(new Vector2(17 * ratio, (progressLine.getHeight() + 17) * ratio));
			progressLine.setPosition(point.x - stage.getRoot().getX(), point.y - stage.getRoot().getY());
			progressLine.setOrigin(0, 0);
			progressLine.setScaleX((float) currentTime / TIME);
		}

		stage.draw();

		// next random part of map
		if (mapWidth + Main.stage.getRoot().getX() < SCREEN_WIDTH + 100)
			randomPart();
	}

	@Override
	public void pause() {
		sndBg.pause();
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();

		// finish load assets
		if (!assetManager.update())
			assetManager.finishLoading();

		bgSound();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		viewport.update(width, height);
		ratio = Math.max((float) viewport.getScreenWidth() / SCREEN_WIDTH, (float) viewport.getScreenHeight() / SCREEN_HEIGHT);

		if (!Gdx.graphics.isFullscreen()) {
			currentWidth = width;
			currentHeight = height;
		}
	}

	@Override
	public void dispose() {
		clearScreen();
		batch.dispose();
		stage.dispose();
		assetManager.clear();
		effectSnow.dispose();

		if (debug != null)
			debug.dispose();

		if (world != null)
			world.dispose();

		System.gc();
	}

	// setSigned
	public void setSigned(boolean signed) {
		isSigned = signed;
		btnSignIn.setVisible(!signed);
		btnSignOut.setVisible(signed);
	}

	// saveScore
	public boolean saveScore(int score) {
		if (!pref.contains("score") || score > pref.getInteger("score")) {
			pref.putInteger("score", score);
			pref.flush();
			return true;
		}

		return false;
	}

	// bgSound
	void bgSound() {
		if (!pref.getBoolean("mute", false)) {
			sndBg.setVolume(0.3f);
			sndBg.setLooping(true);
			sndBg.play();
		}
	}

	// CONTACT
	class CONTACT implements ContactListener {
		@Override
		public void beginContact(Contact contact) {
			Act actor1 = (Act) contact.getFixtureA().getBody().getUserData();
			Act actor2 = (Act) contact.getFixtureB().getBody().getUserData();
			Act otherActor;

			if (hero.enabled) {
				// hero & platform
				if ((actor1.getName().equals("hero") && actor2.getName().equals("platform"))
						|| (actor2.getName().equals("hero") && actor1.getName().equals("platform"))) {
					heroDie();
					return;
				}

				// car & platform
				if ((actor1.getName().equals("car") && actor2.getName().equals("platform"))
						|| (actor2.getName().equals("car") && actor1.getName().equals("platform"))) {
					// sound
					if (car.body.getLinearVelocity().y < -6 && !pref.getBoolean("mute", false)) {
						sndSpring.stop();
						sndSpring.play(0.4f);
					}
					return;
				}

				// car or hero & snowman
				if ((actor1.getName().equals("car") && actor2.getName().equals("snowman"))
						|| (actor2.getName().equals("car") && actor1.getName().equals("snowman"))
						|| (actor1.getName().equals("hero") && actor2.getName().equals("snowman"))
						|| (actor2.getName().equals("hero") && actor1.getName().equals("snowman"))) {
					otherActor = actor1.getName().equals("snowman") ? actor1 : actor2;

					com.badlogic.gdx.physics.box2d.Filter filter = new com.badlogic.gdx.physics.box2d.Filter();
					filter.maskBits = Lib.categoryBits[15];
					otherActor.body.getFixtureList().first().setFilterData(filter);
					otherActor.body.applyLinearImpulse(new Vector2(0, tire0.body.getLinearVelocity().x * 0.1f),
							otherActor.body.getPosition(), true);
					otherActor.body.setAngularVelocity((float) (-10 + Math.random() * 20));
					otherActor.setZIndex(progressBg.getZIndex());

					// sound
					if (!pref.getBoolean("mute", false)) {
						sndSnowman.stop();
						sndSnowman.play(0.8f);
					}
					return;
				}

				if (currentTime > 0) {
					// hero & gift
					if ((actor1.getName().equals("car") && actor2.getName().equals("gift"))
							|| (actor2.getName().equals("car") && actor1.getName().equals("gift"))
							|| (actor1.getName().equals("hero") && actor2.getName().equals("gift"))
							|| (actor2.getName().equals("hero") && actor1.getName().equals("gift"))) {
						otherActor = actor1.getName().equals("gift") ? actor1 : actor2;
						com.badlogic.gdx.physics.box2d.Filter filter = new com.badlogic.gdx.physics.box2d.Filter();
						filter.maskBits = Lib.categoryBits[15];
						otherActor.body.getFixtureList().first().setFilterData(filter);
						otherActor.enabled = false;
						score += 5;

						// save score
						if (saveScore(score))
							nativePlatform.saveScore(score);

						// sound
						if (!pref.getBoolean("mute", false))
							assetManager.get("sndGift.mp3", Sound.class).play(0.5f);

						return;
					}

					// hero & gas
					if ((actor1.getName().equals("car") && actor2.getName().equals("gas"))
							|| (actor2.getName().equals("car") && actor1.getName().equals("gas"))
							|| (actor1.getName().equals("hero") && actor2.getName().equals("gas"))
							|| (actor2.getName().equals("hero") && actor1.getName().equals("gas"))) {
						otherActor = actor1.getName().equals("gas") ? actor1 : actor2;
						if (otherActor.isVisible()) {
							com.badlogic.gdx.physics.box2d.Filter filter = new com.badlogic.gdx.physics.box2d.Filter();
							filter.maskBits = Lib.categoryBits[15];
							otherActor.body.getFixtureList().first().setFilterData(filter);
							otherActor.enabled = false;
							currentTime = TIME;
							progressLine.setScaleX((float) currentTime / TIME);

							// sound
							if (!pref.getBoolean("mute", false))
								assetManager.get("sndGas.mp3", Sound.class).play(0.7f);
						}
						return;
					}
				}
			}
		}

		@Override
		public void endContact(Contact contact) {
			Act actor1 = (Act) contact.getFixtureA().getBody().getUserData();
			Act actor2 = (Act) contact.getFixtureB().getBody().getUserData();

			// snowman & snowman
			if ((actor1.getName().equals("snowman") && actor2.getName().equals("snowman"))) {
				if (Math.abs(actor1.body.getLinearVelocity().x) > 1 || Math.abs(actor1.body.getLinearVelocity().y) > 1
						|| Math.abs(actor2.body.getLinearVelocity().x) > 1 || Math.abs(actor2.body.getLinearVelocity().y) > 1) {
					com.badlogic.gdx.physics.box2d.Filter filter = new com.badlogic.gdx.physics.box2d.Filter();
					filter.maskBits = Lib.categoryBits[15];
					actor1.body.getFixtureList().first().setFilterData(filter);
					actor2.body.getFixtureList().first().setFilterData(filter);
					actor1.body.applyLinearImpulse(new Vector2(0, tire0.body.getLinearVelocity().x * 0.1f),
							actor1.body.getPosition(), true);
					actor2.body.applyLinearImpulse(new Vector2(0, tire0.body.getLinearVelocity().x * 0.1f),
							actor1.body.getPosition(), true);
					actor1.body.setAngularVelocity((float) (-10 + Math.random() * 20));
					actor2.body.setAngularVelocity((float) (-10 + Math.random() * 20));
					actor1.setZIndex(progressBg.getZIndex());
					actor2.setZIndex(progressBg.getZIndex());
				}

				return;
			}
		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
		}
	}

	// CONTROL
	class CONTROL extends InputListener {
		@Override
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			if (((Act) event.getTarget()).enabled) {
				// each button
				if (event.getTarget().getName().substring(0, Math.min(3, event.getTarget().getName().length())).equals("btn")) {
					((Act) event.getTarget()).brightness = BRIGHTNESS_PRESSED;

					// sound
					if (!pref.getBoolean("mute", false))
						assetManager.get("sndBtn.mp3", Sound.class).play(0.9f);
				}

				if (screen.equals("game") && !gamePaused && hero.enabled) {
					// controlLeft
					if (event.getTarget().getName().equals("controlLeft")) {
						keyLeft = true;
						return true;
					}

					// controlRight
					if (event.getTarget().getName().equals("controlRight")) {
						keyRight = true;
						return true;
					}
				}
			}

			return true;
		}

		@Override
		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			super.touchUp(event, x, y, pointer, button);
			if (((Act) event.getTarget()).enabled) {
				// controLeft
				if (event.getTarget().getName().equals("controlLeft")) {
					keyLeft = false;
					return;
				}

				// controlRight
				if (event.getTarget().getName().equals("controlRight")) {
					keyRight = false;
					return;
				}

				// each button
				if (event.getTarget().getName().substring(0, Math.min(3, event.getTarget().getName().length())).equals("btn"))
					((Act) event.getTarget()).brightness = 1;

				// if actor in focus
				if (stage.hit(event.getStageX(), event.getStageY(), true) == event.getTarget()) {
					// btnPause
					if (event.getTarget().getName().equals("btnPause")) {
						gamePaused = true;
						groupPause.setVisible(true);
						btnPause.setVisible(false);
						taskDelay = (TIMER.getExecuteTimeMillis() - TimeUtils.nanosToMillis(TimeUtils.nanoTime())) / 1000f;
						TIMER.cancel();
						return;
					}

					// btnSignIn
					if (event.getTarget().getName().equals("btnSignIn")) {
						nativePlatform.signIn();
						return;
					}

					// btnSignOut
					if (event.getTarget().getName().equals("btnSignOut")) {
						nativePlatform.signOut();
						return;
					}

					// btnLeaders
					if (event.getTarget().getName().equals("btnLeaders")) {
						nativePlatform.showLeaders();
						return;
					}

					// btnStart
					if (event.getTarget().getName().equals("btnStart")) {
						showScreen("game");
						return;
					}

					// btnSound, btnSoundPause
					if (event.getTarget().getName().equals("btnSound") || event.getTarget().getName().equals("btnSoundPause")) {
						pref.putBoolean("mute", false);
						pref.flush();
						btnMute.setVisible(true);
						btnSound.setVisible(false);
						bgSound();
						return;
					}

					// btnMute, btnMutePause
					if (event.getTarget().getName().equals("btnMute") || event.getTarget().getName().equals("btnMutePause")) {
						pref.putBoolean("mute", true);
						pref.flush();
						btnMute.setVisible(false);
						btnSound.setVisible(true);
						sndBg.pause();
						return;
					}

					// btnQuit
					if (event.getTarget().getName().equals("btnQuit")) {
						Gdx.app.exit();
						return;
					}

					// btnRestart, btnRestartPause
					if (event.getTarget().getName().equals("btnRestart") || event.getTarget().getName().equals("btnRestartPause")) {
						showScreen("game");
						return;
					}

					// btnResumePause
					if (event.getTarget().getName().equals("btnResumePause")) {
						gamePaused = false;
						bgSound();
						groupPause.setVisible(false);
						btnPause.setVisible(true);
						if (currentTime > 0)
							Timer.schedule(TIMER, taskDelay, 1);
						return;
					}

					// btnQuitPause, btnBack
					if (event.getTarget().getName().equals("btnQuitPause") || event.getTarget().getName().equals("btnBack")) {
						showScreen("main");
						return;
					}
				}
			}
		}

		@Override
		public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
			super.enter(event, x, y, pointer, fromActor);

			// mouse over button
			if (((Act) event.getTarget()).enabled
					&& event.getTarget().getName().substring(0, Math.min(3, event.getTarget().getName().length())).equals("btn"))
				((Act) event.getTarget()).brightness = BRIGHTNESS_PRESSED;
		}

		@Override
		public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
			super.exit(event, x, y, pointer, toActor);

			// mouse out button
			if (event.getTarget().getName().substring(0, Math.min(3, event.getTarget().getName().length())).equals("btn"))
				((Act) event.getTarget()).brightness = 1;
		}

		@Override
		public boolean keyDown(InputEvent event, int keycode) {
			if (screen.equals("game") && !gamePaused && hero.enabled)
				switch (keycode) {
				case Keys.LEFT:
					keyLeft = true;
					break;
				case Keys.RIGHT:
					keyRight = true;
					break;
				}

			return true;
		}

		@Override
		public boolean keyUp(InputEvent event, int keycode) {
			switch (keycode) {
			case Keys.LEFT:
				keyLeft = false;
				break;
			case Keys.RIGHT:
				keyRight = false;
				break;
			case Keys.ESCAPE: // exit from fullscreen mode
				if (Gdx.graphics.isFullscreen())
					Gdx.graphics.setWindowedMode(currentWidth, currentHeight);
				break;
			case Keys.ENTER: // switch to fullscreen mode
				if (!Gdx.graphics.isFullscreen())
					Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
				break;
			case Keys.BACK: // back
				if (screen.equals("main"))
					Gdx.app.exit();
				else if (screen.equals("game"))
					showScreen("main");
				break;
			}

			return true;
		}
	}

	// log
	void log(Object obj) {
		if (Gdx.app.getType().equals(ApplicationType.Desktop))
			System.out.println(obj);
		else
			Gdx.app.log("@", obj.toString());
	}

	// TIMER
	void TIMER() {
		TIMER = new Task() {
			@Override
			public void run() {
				currentTime--;

				if (currentTime == 0) {
					// time is up
					TIMER.cancel();
					hideElements();

					// sound
					if (!pref.getBoolean("mute", false))
						assetManager.get("sndTimeUp.mp3", Sound.class).play(0.6f);

					// groupTimeUp
					showGroup(groupTimeUp);
				} else if (currentTime <= 5 && !pref.getBoolean("mute", false))
					assetManager.get("sndEmpty.mp3", Sound.class).play(0.3f);
			}
		};
	}

	// heroDie
	void heroDie() {
		if (hero.enabled) {
			hero.enabled = false;
			destroyJoints.add(jointHeroLive);
			destroyJoints.add(jointBag);
			destroyJoints.add(jointTire0);
			TIMER.cancel();
			hideElements();

			if (currentTime != 0) {
				// sound
				if (!pref.getBoolean("mute", false))
					sndGameOver.play(0.5f);

				showGroup(groupGameOver);
			}
		}
	}

	// showGroup
	void showGroup(Group group) {
		// add score numbers
		String str = String.valueOf(score);
		Array<Act> actors = new Array<Act>();
		int numbersWidth = 0;
		for (int i = 0; i < str.length(); i++) {
			Act actor = new Act("", 0, 570, numbers.findRegion(str.substring(i, i + 1)));
			actors.add(actor);
			group.addActor(actor);
			numbersWidth += actor.getWidth();
		}

		// set numbers position
		float x_pos = (SCREEN_WIDTH - numbersWidth) / 2;
		for (int i = 0; i < actors.size; i++) {
			actors.get(i).setX(x_pos);
			x_pos += actors.get(i).getWidth();
		}

		// show
		group.setVisible(true);
		SnapshotArray<Actor> allActors = group.getChildren();
		for (int i = 0; i < allActors.size; i++) {
			allActors.get(i).setScale(0, 0);
            if(i != allActors.size - 1)
			    allActors.get(i).addAction(
					Actions.sequence(Actions.delay(2f + i * 0.2f), Actions.scaleBy(1, 1, 1, Interpolation.elasticOut)));
            else
                allActors.get(i).addAction(
                        Actions.sequence(Actions.delay(2f + i * 0.2f), Actions.scaleBy(1, 1, 1, Interpolation.elasticOut), new Action() {
                            @Override
                            public boolean act(float delta) {
                                // show AdMob Interstitial
                                nativePlatform.admobInterstitial();
                                return true;
                            }
                        }));
		}
	}

	// randomPart
	void randomPart() {
		// remove left actors
		for (int i = 0; i < leftActors.size; i++) {
			if (leftActors.get(i).body != null)
				destroyBodies.add(leftActors.get(i).body);
			leftActors.get(i).remove();
		}
		leftActors = screenActors;

		// next part
		int rand = (int) Math.round(Math.random() * 12);
		screenActors = Lib.addLayers("bg", map, stage.getRoot());
		screenActors.addAll(Lib.addLayers("" + rand, map, stage.getRoot()));
		screenActors.reverse();

		for (int i = 0; i < screenActors.size; i++) {
			// index
			screenActors.get(i).setZIndex(0);

			// position
			if (screenActors.get(i).body == null)
				screenActors.get(i).setX(screenActors.get(i).getX() + mapWidth);
			else
				screenActors.get(i).body.setTransform(screenActors.get(i).body.getPosition().x + mapWidth / PPM,
						screenActors.get(i).body.getPosition().y, screenActors.get(i).body.getAngle());
		}

		// mapWidth
		mapWidth += PART_WIDTH;
		numPart++;

		if (numPart >= 3) {
			// leftLimit
			leftLimit += PART_WIDTH;

			// move forward start part
			for (int i = 0; i < startActors.size; i++)
				if (startActors.get(i).body == null)
					startActors.get(i).setX(startActors.get(i).getX() + PART_WIDTH);
				else
					startActors.get(i).body.setTransform(startActors.get(i).body.getPosition().x + PART_WIDTH / PPM,
							startActors.get(i).body.getPosition().y, startActors.get(i).body.getAngle());
		}
	}

	// hideElements
	void hideElements() {
		// remove listeners
		controlLeft.removeListener(controlListener);
		controlRight.removeListener(controlListener);
		btnPause.removeListener(controlListener);

		progressBg.addAction(Actions.sequence(Actions.alpha(0, 0.2f), new Action() {
			@Override
			public boolean act(float delta) {
				progressBg.setVisible(false);
				return false;
			}
		}));

		progressLine.addAction(Actions.sequence(Actions.alpha(0, 0.2f), new Action() {
			@Override
			public boolean act(float delta) {
				progressLine.setVisible(false);
				return false;
			}
		}));

		progressOver.addAction(Actions.sequence(Actions.alpha(0, 0.2f), new Action() {
			@Override
			public boolean act(float delta) {
				progressOver.setVisible(false);
				return false;
			}
		}));

		btnPause.addAction(Actions.sequence(Actions.alpha(0, 0.2f), new Action() {
			@Override
			public boolean act(float delta) {
				btnPause.setVisible(false);
				return false;
			}
		}));
	}
}