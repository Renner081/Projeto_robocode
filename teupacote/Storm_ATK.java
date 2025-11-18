//** PARTICIPANTES DA EQUIPE : Renner luiz, Guilherme fernandes, Daniel guilherme, Erick felipe.**//
package teupacote;
import robocode.*;
import java.awt.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;


public class Storm_ATK extends AdvancedRobot {

    int moveDirection = 1;

    public void run() {
        setColors(Color.GREEN, Color.GRAY, Color.red); 

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            turnRadarRight(360);  // Radar sempre girando
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        double enemyEnergy = e.getEnergy();

        // ----- DETECTA TIRO -----
        if (enemyEnergy < getEnergy() && enemyEnergy <= 3) {
            moveDirection = -moveDirection;  // Inverte movimento quando ele atira
        }

        // ----- MOVIMENTO EVASIVO -----
        setTurnRight(e.getBearing() + 90 - 30 * moveDirection);  
        setAhead(150 * moveDirection);

        // ----- WALL SMOOTHING -----
        wallSmooth();

        // ----- MIRA PREDITIVA (Tiro Linear) -----
        double bulletPower = getBulletPower(e.getDistance());
        double enemyVelocity = e.getVelocity();
        double enemyHeading = e.getHeading();

        double fireTime = e.getDistance() / (20 - bulletPower * 3);
        double predictedX = getX() + Math.sin(Math.toRadians(e.getBearing() + getHeading())) * e.getDistance();
        double predictedY = getY() + Math.cos(Math.toRadians(e.getBearing() + getHeading())) * e.getDistance();

        predictedX += Math.sin(Math.toRadians(enemyHeading)) * enemyVelocity * fireTime;
        predictedY += Math.cos(Math.toRadians(enemyHeading)) * enemyVelocity * fireTime;

        double angleToPredicted = Math.toDegrees(Math.atan2(predictedX - getX(), predictedY - getY()));
        double gunTurn = normalRelativeAngleDegrees(angleToPredicted - getGunHeading());

        setTurnGunRight(gunTurn);

        if (Math.abs(gunTurn) < 6) {
            setFire(bulletPower);
        }
    }

    // ----- MÉTODO WALL SMOOTHING -----
    private void wallSmooth() {
        if (getX() < 50 || getX() > getBattleFieldWidth() - 50 || 
            getY() < 50 || getY() > getBattleFieldHeight() - 50) {
            setTurnRight(90);
            setAhead(100);
        }
    }

    // ----- MÉTODO PARA DEFINIR POTÊNCIA DO TIRO -----
    private double getBulletPower(double distance) {
        if (distance > 200) return 1.0;
        if (distance > 50) return 2.0;
        return 3.0;
    }
}
//** PARTICIPANTES : Renner luiz, Guilherme fernandes, Daniel guilherme, Erick felipe.**//