package br.com.gabrielcouto.ledtec.homepage.fragments;

import br.com.gabrielcouto.ledtec.model.entity.LuminariaLugar;

public interface Comunicador {
    void event(LuminariaLugar father);
}
