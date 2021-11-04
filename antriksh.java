package antriksh;

import robocode.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;

/*
 * antriksh 1.0
 *
 * By Antriksh Arora
 */
public class antriksh extends AdvancedRobot {

    double fieldWidth; // battlefield width
    double fieldHeight; // battlefield height
    boolean forwardMovement; // is true when robot is moving forward - when ahead is called, is false when back is called
    boolean closeToWall; // is true when robot is closer than 50px to any wall
    int missedBullets = 0; // counts the streak in which bullets don't hit any enemy
    int counter = 0; // counts whether bullet should be shot or not after a long miss streak

    public void run() {

        // get field width and height
        fieldWidth = getBattleFieldWidth();
        fieldHeight= getBattleFieldHeight();

        // allow every part of the robot move independent of the other parts
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        // check if the robot os closer than 50px to any wall
        if (getX() <= 50 || getY() <= 50 || fieldWidth-getX() <= 50 || fieldHeight-getY() <= 50) {
            closeToWall = true;
        } else {
            closeToWall = false;
        }

        setAhead(100000); // lage number to make it move infinity unit it gets interrupted
        forwardMovement = true;
        setTurnRadarRight(360); // scans around himself to spot enemies

        while (true) {

            // if the robot was close a wall, check if it has moved away yet and set the variable to false
            if (getX() > 50 && getY() > 50 && getBattleFieldWidth() - getX() > 50 && getBattleFieldHeight() - getY() > 50 && closeToWall == true) {
                closeToWall = false;
            }

            // check if robot is close to any wall
            if (getX() <= 50 || getY() <= 50 || fieldWidth - getX() <= 50 || fieldHeight - getY() <= 50 ) {
                // if hasn't noticed yet, then move robot away and change the value of the variable to true
                // if has noticed yet, then do nothing
                if (closeToWall == false){
                    bounce();
                    closeToWall = true;
                }
            }

            // in case the radar stopped turning, scan around and spot a new enemy
            if (getRadarTurnRemaining() == 0.0){
                setTurnRadarRight(360);
            }

            execute();
        }
    }

    // when robot aims at an enemy
    public void onScannedRobot(ScannedRobotEvent event) {

        // get location of the enemy
        double absoluteBearing = getHeading() + event.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
        double bearingFromRadar = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading());

        // movement: move around enemy in circle
        if (forwardMovement){
            setTurnRight(normalRelativeAngleDegrees(event.getBearing() + 80));
        }
        // if is moving backwards, add 100 - 80 and 100 causes that robot moves a bit closer after hitting wall
        else {
            setTurnRight(normalRelativeAngleDegrees(event.getBearing() + 100));
        }

        // fire if the enemy is close enough
        if (Math.abs(bearingFromGun) <= 3) {
            setTurnGunRight(bearingFromGun); // turns gun to keep aim at the enemy
            setTurnRadarRight(bearingFromRadar); // turns the radar to keep track of the enemy

            // check gun heat and energy to not kill himself
            if (getGunHeat() == 0 && getEnergy() > 15) {
                // the closer the enemy, the more powerful the bullet
                fire(Math.min(3 - Math.abs(bearingFromGun) / 2 - event.getDistance() / 250, getEnergy() - .1));
            }
        } else {
            // else just keep track the enemy
            setTurnGunRight(bearingFromGun);
            setTurnRadarRight(bearingFromRadar);
        }

    }

    // in case the robot still touches the wall
    public void onHitWall(HitWallEvent event) {
        bounce();
    }

    public void bounce() {
        if (forwardMovement) {
            setBack(100000);
            forwardMovement = false;
        } else {
            setAhead(100000);
            forwardMovement = true;
        }
    }


    public void onHitByBullet(HitByBulletEvent event) {
        // TODO: take action to dodge bullets
    }


    public void onHitRobot(HitRobotEvent event) {
        // if we drive into another robot, then bounce - when another robot drives into us, do nothing
        if (event.isMyFault()) {
            bounce();
        }
        // TODO: if the robot hits us in our moving direction, then bounce - if it hits us from an other direction, just continue moving
    }


    public void onBulletHit(BulletHitEvent event) {
        // reset the missed bullets streak
        missedBullets = 0;
        counter = 0;
    }


    public void onBulletMissed(BulletMissedEvent event) {
        // add one to the missed bullets streak
        missedBullets++;
    }


    public void onWin(WinEvent event) {
        // do a win dance
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }

    // TODO: predict the enemies movement to miss less bullets
}
