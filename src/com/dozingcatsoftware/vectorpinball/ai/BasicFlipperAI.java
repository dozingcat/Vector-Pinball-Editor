package com.dozingcatsoftware.vectorpinball.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.dozingcatsoftware.vectorpinball.elements.FlipperElement;
import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.Point;

public class BasicFlipperAI implements FlipperAI {

    double triggerRadius;

    public BasicFlipperAI(double triggerRadius) {
        this.triggerRadius = triggerRadius;
    }

    @Override public void updateFlippers(Field field) {
        boolean left = false;
        boolean right = false;
        for (FlipperElement flipper : field.getFlipperElements()) {
            Vector2 origin = flipper.getAnchorBody().getPosition();
            double angle = flipper.getJoint().getJointAngle();
            double length = flipper.getFlipperLength();
            double endx = origin.x + length*Math.cos(angle);
            double endy = origin.y + length*Math.sin(angle);

            for (Body ball : field.getBalls()) {
                Vector2 ballPos = ball.getPosition();
                if (Point.distanceBetween(ballPos.x, ballPos.y, endx, endy) <= triggerRadius) {
                    if (flipper.isLeftFlipper()) {
                        left = true;
                    }
                    else {
                        right = true;
                    }
                    break;
                }
            }
        }
        field.setLeftFlippersEngaged(left);
        field.setRightFlippersEngaged(right);
    }

}
