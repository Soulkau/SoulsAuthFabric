package com.soulkau.authmefantomasik.classes;

import net.minecraft.util.Identifier;

import java.util.StringJoiner;

public class Position {
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    private String worldpath;

    private String worldnamespace;



    public Position(double x, double y, double z, float yaw, float pitch, String worldpath, String worldnamespace) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.worldpath = worldpath;
        this.worldnamespace = worldnamespace;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public String getWorldpath() {
        return worldpath;
    }

    public String getWorldnamespace() {
        return worldnamespace;
    }

    public static Position getPositionFromJson(String json) {
        String[] location = json.split("::");

        double x = Double.parseDouble(location[0]);
        double y = Double.parseDouble(location[1]);
        double z = Double.parseDouble(location[2]);
        float yaw = Float.parseFloat(location[3]);
        float pitch = Float.parseFloat(location[4]);
        String worldPath = location[5];
        String worldNamespace = location[6];

        System.out.println("New position");
        return new Position(x, y, z, yaw, pitch, worldPath, worldNamespace);
    }

    public Identifier getWorldIdentifier(Position pos) {
        return new Identifier(pos.getWorldnamespace(), pos.getWorldpath());
    }

    public String turnToJson() {
        StringJoiner stringJoiner = new StringJoiner("::");
        stringJoiner.add(String.valueOf(x)).add(String.valueOf(y)).add(String.valueOf(z)).add(String.valueOf(yaw)).add(String.valueOf(pitch)).add(worldpath).add(worldnamespace);
        return stringJoiner.toString();
    }



}
