package com.demo.game.objects.dao;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TRANSPORT")
public class Transport extends Unit {

}


