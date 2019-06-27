package br.com.gabrielcouto.ledtec.model.entity;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "lumPlace")
public class LuminariaLugar implements Serializable {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField
    private String nome;

    @DatabaseField
    private String icone;

    @DatabaseField
    private Integer level;

    @DatabaseField
    private String mac;

    @DatabaseField
    private Integer end1;

    @DatabaseField
    private Integer end2;

    @DatabaseField
    private Integer end3;

    @DatabaseField
    private Integer idLevel;

    @DatabaseField(foreign = true,columnName = "lumPlace_id",canBeNull = true)
    private LuminariaLugar father;

    public LuminariaLugar(){

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Integer getEnd1() {
        return end1;
    }

    public void setEnd1(Integer end1) {
        this.end1 = end1;
    }

    public Integer getEnd2() {
        return end2;
    }

    public void setEnd2(Integer end2) {
        this.end2 = end2;
    }

    public Integer getEnd3() {
        return end3;
    }

    public void setEnd3(Integer end3) {
        this.end3 = end3;
    }

    public Integer getIdLevel() {
        return idLevel;
    }

    public void setIdLevel(Integer idLevel) {
        this.idLevel = idLevel;
    }

    public LuminariaLugar getFather() {
        return father;
    }

    public void setFather(LuminariaLugar father) {
        this.father = father;
    }
}
