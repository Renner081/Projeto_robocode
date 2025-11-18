//**PARTICIPANTES DA EQUIPE: Renner luiz, Guilherme fernandes, Daniel guilherme, Erick felipe.**//
package teupacote;
import robocode.*;
import java.awt.*;
import java.util.ArrayList;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class STORM_DEF extends AdvancedRobot {

    // ==========================
    //        WAVE CLASS
    // ==========================
    public class EnemyWave {
        public double startX, startY;
        public double fireTime;
        public double bulletVelocity;
        public double directAngle;
        public double distanceTraveled;
    }

    ArrayList<EnemyWave> waves = new ArrayList<>();
    double lastEnemyEnergy = 100;
    int moveDirection = 1;

    // Tabela de risco (GuessFactor movement)
    static int BINS = 47;
    double[] danger = new double[BINS];

    @Override
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setScanColor(Color.RED);
        setBodyColor(Color.BLACK);
        setGunColor(Color.DARK_GRAY);

        setTurnRadarRight(360);

        for (int i = 0; i < BINS; i++) danger[i] = 1;

        while (true) {
            surfWaves();
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {

        // ==========================
        //    DETECTA TIRO & CRIA ONDA
        // ==========================
        double energyDrop = lastEnemyEnergy - e.getEnergy();
        if (energyDrop > 0 && energyDrop <= 3.0) {
            EnemyWave ew = new EnemyWave();
            ew.startX = getX();
            ew.startY = getY();
            ew.directAngle = getHeadingRadians() + Math.toRadians(e.getBearing());
            ew.fireTime = getTime();
            ew.bulletVelocity = 20 - 3 * energyDrop;
            waves.add(ew);
        }
        lastEnemyEnergy = e.getEnergy();

        // ==========================
        //         RADAR LOCK
        // ==========================
        double radarTurn =
                normalRelativeAngleDegrees(getHeading() + e.getBearing() - getRadarHeading());
        setTurnRadarRight(radarTurn * 2);

        // ==========================
        //          MIRA LINEAR
        // ==========================
        double gunTurn = aimLinear(e);
        setTurnGunRight(gunTurn);

        if (Math.abs(gunTurn) < 6)
            setFire(2);
    }

    // ==========================
    //      LINEAR AIM
    // ==========================
    private double aimLinear(ScannedRobotEvent e) {
        double absoluteBearing = getHeading() + e.getBearing();
        double power = 2;
        double bulletSpeed = 20 - 3 * power;

        double enemyHeading = e.getHeading();
        double enemyVelocity = e.getVelocity();

        double fireTime = e.getDistance() / bulletSpeed;

        double futureX = getX() + Math.sin(Math.toRadians(absoluteBearing)) * e.getDistance();
        double futureY = getY() + Math.cos(Math.toRadians(absoluteBearing)) * e.getDistance();

        futureX += Math.sin(Math.toRadians(enemyHeading)) * enemyVelocity * fireTime;
        futureY += Math.cos(Math.toRadians(enemyHeading)) * enemyVelocity * fireTime;

        double angle = Math.toDegrees(Math.atan2(
                futureX - getX(),
                futureY - getY()
        ));

        return normalRelativeAngleDegrees(angle - getGunHeading());
    }

    // ==========================
    //         SURFAR ONDAS
    // ==========================
    private void surfWaves() {
        EnemyWave closestWave = getClosestWave();
        if (closestWave == null) return;

        double dangerLeft = checkDanger(closestWave, -1);
        double dangerRight = checkDanger(closestWave, 1);

        int direction = (dangerLeft < dangerRight) ? -1 : 1;

        moveDirection = direction;

        // Movimento suave + anti-fire
        setTurnRightRadians(normalRelativeAngleDegrees(
                Math.toDegrees(closestWave.directAngle + (direction * Math.PI / 2))
        ));
        setAhead(150 * direction);
    }

    private EnemyWave getClosestWave() {
        EnemyWave best = null;
        double minDistance = Double.MAX_VALUE;

        for (EnemyWave ew : waves) {
            double traveled = (getTime() - ew.fireTime) * ew.bulletVelocity;
            double distance = Math.abs(traveled - getRange(
                    ew.startX, ew.startY, getX(), getY()
            ));

            if (distance < minDistance) {
                minDistance = distance;
                best = ew;
            }
        }
        return best;
    }

    // ==========================
    //       CALCULO DE RISCO
    // ==========================
    private double checkDanger(EnemyWave wave, int direction) {

        double angleOffset = direction * Math.PI / 2;
        double testX = getX() + Math.sin(getHeadingRadians() + angleOffset) * 60;
        double testY = getY() + Math.cos(getHeadingRadians() + angleOffset) * 60;

        double guess = getGuessFactor(wave, testX, testY);

        int index = (int) ((BINS - 1) * (guess + 1) / 2);
        index = Math.max(0, Math.min(BINS - 1, index));

        return danger[index];
    }

    private double getGuessFactor(EnemyWave w, double x, double y) {
        double angle = Math.atan2(x - w.startX, y - w.startY);
        double offset = normalRelativeAngleRadians(angle - w.directAngle);
        double maxEscapeAngle = Math.asin(8 / w.bulletVelocity);

        return offset / maxEscapeAngle;
    }

    private double getRange(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    private double normalRelativeAngleRadians(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
}
//** PARTICIPANTES : Renner luiz, Guilherme fernandes, Daniel guilherme, Erick felipe.**//