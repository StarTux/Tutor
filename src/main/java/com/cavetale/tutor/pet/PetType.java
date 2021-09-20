package com.cavetale.tutor.pet;

import com.cavetale.mytems.Mytems;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Wolf;

@RequiredArgsConstructor
public enum PetType {
    CAT(Component.text("Cat"), Noise.CAT, Mytems.PIC_CAT, true, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.BLACK);
                });
        }
    },
    DOG(Component.text("Dog"), Noise.DOG, Mytems.PIC_WOLF, true, "woof woof") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Wolf.class, dog -> {
                    callback.accept(dog);
                    dog.setCollarColor(DyeColor.LIGHT_BLUE);
                });
        }
    },
    BLACK_CAT(Component.text("Black Cat"), Noise.CAT, Mytems.PIC_CAT, false, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.ALL_BLACK);
                });
        }
    },
    SHORTHAIR_CAT(Component.text("Shorthair Cat"), Noise.CAT, Mytems.PIC_CAT, false, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.BRITISH_SHORTHAIR);
                });
        }
    },
    CALICO_CAT(Component.text("Calico Cat"), Noise.CAT, Mytems.PIC_CAT, false, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.CALICO);
                });
        }
    },
    JELLIE_CAT(Component.text("Jellie Cat"), Noise.CAT, Mytems.PIC_CAT, false, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.JELLIE);
                });
        }
    },
    PERSIAN_CAT(Component.text("Persian Cat"), Noise.CAT, Mytems.PIC_CAT, false, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.PERSIAN);
                });
        }
    },
    RAGDOLL_CAT(Component.text("Ragdoll Cat"), Noise.CAT, Mytems.PIC_CAT, false, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.RAGDOLL);
                });
        }
    },
    RED_CAT(Component.text("Red Cat"), Noise.CAT, Mytems.PIC_CAT, false, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.RED);
                });
        }
    },
    SIAMESE_CAT(Component.text("Siamese Cat"), Noise.CAT, Mytems.PIC_CAT, false, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.SIAMESE);
                });
        }
    },
    TABBY_CAT(Component.text("Tabby Cat"), Noise.CAT, Mytems.PIC_CAT, false, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.TABBY);
                });
        }
    },
    WHITE_CAT(Component.text("White Cat"), Noise.CAT, Mytems.PIC_CAT, false, "meow meow") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cat.class, cat -> {
                    callback.accept(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.WHITE);
                });
        }
    },
    SHEEP(Component.text("Baby Sheep"), Noise.FAIL, Mytems.QUESTION_MARK, false, "baa") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Sheep.class, sheep -> {
                    callback.accept(sheep);
                    sheep.setBaby();
                });
        }
    },
    SLIME(Component.text("Lil' Slime"), Noise.FAIL, Mytems.QUESTION_MARK, false, "flup flup") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Slime.class, slime -> {
                    callback.accept(slime);
                    slime.setSize(1);
                });
        }
    },
    GOAT(Component.text("Lil' Goat"), Noise.FAIL, Mytems.QUESTION_MARK, false, "baa") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Goat.class, goat -> {
                    callback.accept(goat);
                    goat.setBaby();
                });
        }
    },
    TURTLE(Component.text("Turtle"), Noise.FAIL, Mytems.QUESTION_MARK, false, "grumble") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Turtle.class, turtle -> {
                    callback.accept(turtle);
                });
        }
    },
    CHICKEN(Component.text("Chicken"), Noise.FAIL, Mytems.QUESTION_MARK, false, "bawk") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Chicken.class, chicken -> {
                    callback.accept(chicken);
                });
        }
    },
    PIG(Component.text("Baby Pig"), Noise.FAIL, Mytems.QUESTION_MARK, false, "oink") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Pig.class, pig -> {
                    callback.accept(pig);
                    pig.setBaby();
                });
        }
    },
    COW(Component.text("Baby Cow"), Noise.FAIL, Mytems.QUESTION_MARK, false, "mooo") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Cow.class, cow -> {
                    callback.accept(cow);
                    cow.setBaby();
                });
        }
    },
    RED_PARROT(Component.text("Red Parrot"), Noise.FAIL, Mytems.QUESTION_MARK, false, "squawk") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Parrot.class, parrot -> {
                    callback.accept(parrot);
                    parrot.setVariant(Parrot.Variant.RED);
                });
        }
    },
    GREEN_PARROT(Component.text("Green Parrot"), Noise.FAIL, Mytems.QUESTION_MARK, false, "squawk") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Parrot.class, parrot -> {
                    callback.accept(parrot);
                    parrot.setVariant(Parrot.Variant.GREEN);
                });
        }
    },
    GRAY_PARROT(Component.text("Gray Parrot"), Noise.FAIL, Mytems.QUESTION_MARK, false, "squawk") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Parrot.class, parrot -> {
                    callback.accept(parrot);
                    parrot.setVariant(Parrot.Variant.GRAY);
                });
        }
    },
    CYAN_PARROT(Component.text("Cyan Parrot"), Noise.FAIL, Mytems.QUESTION_MARK, false, "squawk") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Parrot.class, parrot -> {
                    callback.accept(parrot);
                    parrot.setVariant(Parrot.Variant.CYAN);
                });
        }
    },
    BLUE_PARROT(Component.text("Blue Parrot"), Noise.FAIL, Mytems.QUESTION_MARK, false, "squawk") {
        @Override public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
            return location.getWorld().spawn(location, Parrot.class, parrot -> {
                    callback.accept(parrot);
                    parrot.setVariant(Parrot.Variant.BLUE);
                });
        }
    };

    public final String key = name().toLowerCase();
    public final Component displayName;
    public final Noise voice;
    public final Mytems mytems;
    public final boolean unlocked;
    public final String speechGimmick;

    public LivingEntity spawn(Location location, Consumer<LivingEntity> callback) {
        return null;
    }
}
