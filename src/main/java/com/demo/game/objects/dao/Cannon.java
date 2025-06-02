package com.demo.game.objects.dao;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CANNON")
public class Cannon extends Unit {

}


