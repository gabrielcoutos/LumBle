package br.com.gabrielcouto.ledtec.utils;

public abstract class ConstBle {

    public static final Integer GRAVAR_ENDERECO = 102;
    public static final Integer GRAVAR_LUZ_ALTA = 101;
    public static final Integer GRAVAR_LUZ_BAIXA = 106;


    /***
     * Serviços
     */
    public static final String SERVICE_GENERICO_RGB = "0003cbbb-0000-1000-8000-00805f9b0131";
    public static final String SERVICE_GERAL = "38A23AAC-11A1-4652-B3C4-AA7C767B216D";
    public static final String SERVICE_TIME = "38A23AAC-11A1-4652-B3C4-AA7C767B216D";


    /**
     * Caracteristicas
     * */
    public static final String CHARACTERISTIC_GENERICO_RGB = "0003cbb1-0000-1000-8000-00805f9b0131";
    public static final String CHARACTERISTIC_LER_INFORMACOES = "FFB2F272-320E-42DB-A9BE-B13351577DC6";
    public static final String CHARACTERISTIC_LER_VERSAO = "CD3E1EDA-6612-4AFF-BB36-77B3F61F01A9";
    public static final String CHARACTERISTIC_GRAVAR_LUZ_ALTA = "a2bcf731-c121-476a-ba7a-9f14179d2169";
    public static final String CHARACTERISTIC_GRAVAR_LUZ_BAIXA = "56742111-64cb-4975-8318-0ef09b6ca3cf";
    public static final String NOTIFICACAO_LUMINOSIDADE = "8FDFE0A6-3935-440B-9A4E-67B21B8F6855";
}
