package com.dozingcatsoftware.vectorpinball.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.physics.box2d.Body;
import com.dozingcatsoftware.vectorpinball.editor.JarFileFieldReader;
import com.dozingcatsoftware.vectorpinball.model.Field;

public class CliMain {

    static class SimResult {
        public final long score;
        public final long ticks;

        public SimResult(long score, long ticks) {
            this.score = score;
            this.ticks = ticks;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Processors: " + Runtime.getRuntime().availableProcessors());
        long t1 = System.currentTimeMillis();
        int fieldNum = 4;
        Map<String, Object> fieldMap = (new JarFileFieldReader()).layoutMapForBuiltInField(fieldNum);

        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i=0; i<=40; i++) {
            double dist = 0.5 + 0.05*i;
            executor.submit(() -> {
                List<SimResult> results = runWithFieldParams(fieldMap, 100, dist);
                System.out.printf("Distance:%.2f median:%d mean:%d\n", dist, medianScore(results), meanScore(results));
            });
        }
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.MINUTES);
        long t2 = System.currentTimeMillis();
        System.out.println("Done, elapsed ms: " + (t2-t1));
    }

    static List<SimResult> runWithFieldParams(Map<String, Object> fieldMap, int iters, double dist) {
        Field field = new Field();
        field.resetForLevel(fieldMap);
        List<SimResult> results = runIters(iters, field, new BasicFlipperAI(dist));
        return results;
    }

    static List<SimResult> runIters(int iters, Field field, FlipperAI ai) {
        List<SimResult> results = new ArrayList<>();
        for (int i=0; i<iters; i++) {
            results.add(runGame(field, 60.0, ai));
        }
        return results;
    }

    static long medianScore(List<SimResult> results) {
        Collections.sort(results, (a, b) -> Long.valueOf(a.score).compareTo(b.score));
        return results.get(results.size() / 2).score;
    }

    static long meanScore(List<SimResult> results) {
        long total = 0;
        for (SimResult s : results) {
            total += s.score;
        }
        return total / results.size();
    }

    static SimResult runGame(Field field, double targetFps, FlipperAI flipperAI) {
        long noScoreTickLimit = 10000;
        long totalTicks = 0;
        long t1 = System.currentTimeMillis();

        long nanosPerFrame = (long)(1e9 / targetFps);
        long fieldTickNanos = (long)(nanosPerFrame*field.getTargetTimeRatio());

        field.startGame();
        long score = 0;
        String message = "";

        // TODO: Handle ball getting stuck/failing to launch.
        while (field.getGameState().isGameInProgress()) {
            long ticksSinceLastScore = 0;
            //System.out.println("Launching ball " + field.getGameState().getBallNumber() + ": " + field.getScore());
            field.launchBall();
            while (field.getBalls().size() > 0) {
                field.tick(fieldTickNanos, 4);
                long newScore = field.getScore();
                String newMessage = (field.getGameMessage() != null) ? field.getGameMessage().text : "";
                if (!newMessage.equals(message) && !newMessage.equals("")) {
                    //System.out.println(newMessage);
                }
                if (score != newScore) {
                    //System.out.println(newScore + ": " + ticksSinceLastScore);
                    ticksSinceLastScore = 0;
                }
                score = newScore;
                message = newMessage;
                ticksSinceLastScore += 1;
                totalTicks += 1;
                if (ticksSinceLastScore >= noScoreTickLimit) {
                    //System.out.println("LOCK");
                    for (Body ball : new ArrayList<>(field.getBalls())) {
                        field.removeBall(ball);
                    }
                }
                flipperAI.updateFlippers(field);
            }
            //System.out.println("Ball lost");
        }
        long t2 = System.currentTimeMillis();
        long tps = (1000 * totalTicks) / (t2-t1);
        //System.out.println("Game over, score:" + field.getScore() + ", ticks:" + totalTicks + ", ms:" + (t2-t1) + ", tps:" + tps);
        return new SimResult(field.getScore(), totalTicks);
    }

}
