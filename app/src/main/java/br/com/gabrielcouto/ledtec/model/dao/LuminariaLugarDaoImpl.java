package br.com.gabrielcouto.ledtec.model.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

import br.com.gabrielcouto.ledtec.model.entity.LuminariaLugar;

public class LuminariaLugarDaoImpl extends BaseDaoImpl<LuminariaLugar,Integer> implements LuminariaLugarDao {
    public LuminariaLugarDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, LuminariaLugar.class);
    }
}
