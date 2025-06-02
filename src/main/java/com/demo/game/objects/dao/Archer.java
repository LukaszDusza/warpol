package com.demo.game.objects.dao;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ARCHER")
public class Archer extends Unit {

}


