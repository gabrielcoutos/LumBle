package br.com.gabrielcouto.ledtec.controle.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;

import br.com.gabrielcouto.ledtec.R;
import br.com.gabrielcouto.ledtec.controle.ControleLuminaria;
import br.com.gabrielcouto.ledtec.utils.Const;
import br.com.gabrielcouto.ledtec.utils.ConstBle;

public class ConfigLuminariaFragment extends Fragment implements View.OnClickListener , SeekBar.OnSeekBarChangeListener {

    private View rootView;
    private ControleLuminaria control;
    private EditText valorLuzAlta,valorLuzBaixa;
    private CheckBox manterLuzBaixa;
    private Button salvarConfigTempo,maisLuzBaixa,maisLuzAlta,menosLuzBaixa,menosLuzAlta,salvarConfigLuminosidadeAlta,salvarConfigLuminosidadeBaixa;
    private SeekBar luzAlta,luzBaixa;
    private TextView porcentagemLuzAlta,porcentagemLuzBaixa;
    private ConstraintLayout layoutConfigTempo,layoutConfigLuminosidadeAlta,layoutConfigLuminosidadeBaixa;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_configuracao_luminaria,null);
        initInstances();
        initScreen();
        return rootView;
    }
    private void initInstances(){
        valorLuzBaixa = rootView.findViewById(R.id.editTextConfigLuzBaixa);
        valorLuzAlta = rootView.findViewById(R.id.editTextConfigLuzAlta);
        manterLuzBaixa = rootView.findViewById(R.id.checkBoxConfigManterLuzBaixa);
        salvarConfigTempo = rootView.findViewById(R.id.btnSalvarConfigTempo);
        salvarConfigTempo.setOnClickListener(this);
        salvarConfigLuminosidadeAlta = rootView.findViewById(R.id.btnSalvarConfigLuminosidadeAlta);
        salvarConfigLuminosidadeAlta.setOnClickListener(this);
        salvarConfigLuminosidadeBaixa = rootView.findViewById(R.id.btnSalvarConfigLuminosidadeBaixa);
        salvarConfigLuminosidadeBaixa.setOnClickListener(this);
        maisLuzAlta = rootView.findViewById(R.id.btnPlusConfigLuzAlta);
        maisLuzAlta.setOnClickListener(this);
        maisLuzBaixa = rootView.findViewById(R.id.btnPlusConfigLuzBaixa);
        maisLuzBaixa.setOnClickListener(this);
        menosLuzAlta = rootView.findViewById(R.id.btnConfigMenosLuzAlta);
        menosLuzAlta.setOnClickListener(this);
        menosLuzBaixa = rootView.findViewById(R.id.btnMenosConfigLuzBaixa);
        menosLuzBaixa.setOnClickListener(this);
        luzAlta = rootView.findViewById(R.id.seekBarConfigLuzAlta);
        luzBaixa = rootView.findViewById(R.id.seekBarConfigLuzBaixa);
        porcentagemLuzAlta = rootView.findViewById(R.id.textViewPorcentagemConfigLuzAlta);
        porcentagemLuzBaixa = rootView.findViewById(R.id.textViewPorcentagemConfigLuzBaixa);
        layoutConfigTempo = rootView.findViewById(R.id.layoutConfigTempo);
        layoutConfigLuminosidadeAlta = rootView.findViewById(R.id.layoutConfigLuminosidadeAlta);
        layoutConfigLuminosidadeBaixa = rootView.findViewById(R.id.layoutConfigLuminosidadeBaixa);
        control = (ControleLuminaria) getActivity();
    }
    private void initScreen(){
        valorLuzAlta.setText((control.intensidadeAtual[Const.POS_LUZ_ALTA]).toString());
        valorLuzBaixa.setText((control.intensidadeAtual[Const.POS_LUZ_BAIXA]).toString());
        luzAlta.setProgress((control.intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_ALTA]));
        luzBaixa.setProgress(control.intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_BAIXA]);
        luzAlta.setOnSeekBarChangeListener(this); // listeners sao instanciados aqui para nao acender a luminária ao trocar de tela
        luzBaixa.setOnSeekBarChangeListener(this);
        porcentagemLuzAlta.setText((control.intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_ALTA]).toString());
        porcentagemLuzBaixa.setText((control.intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_BAIXA]).toString());
        manterLuzBaixa.setChecked(control.intensidadeAtual[Const.POS_MANTER_LUZ_BAIXA] ==1 ? true:false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSalvarConfigTempo:
                enviandoComando();
                enviarComandoSalvarConfigTempoLuzAlta();
                break;
            case R.id.btnSalvarConfigLuminosidadeAlta:
                enviandoComando();
                enviarComandoSalvarConfigLuminosidadeAlta();
                break;
            case R.id.btnSalvarConfigLuminosidadeBaixa:
                enviandoComando();
                enviarComandoSalvarConfigLuminosidadeBaixa();
                break;
            case R.id.btnPlusConfigLuzAlta:
                if(luzAlta.getProgress()<100)
                    luzAlta.setProgress(luzAlta.getProgress()+1);
                break;
            case R.id.btnConfigMenosLuzAlta:
                if(luzAlta.getProgress()>0)
                    luzAlta.setProgress(luzAlta.getProgress()-1);
                break;
            case R.id.btnPlusConfigLuzBaixa:
                if(luzBaixa.getProgress()<100)
                    luzBaixa.setProgress(luzBaixa.getProgress()+1);
                break;
            case R.id.btnMenosConfigLuzBaixa:
                if(luzBaixa.getProgress()>0)
                    luzBaixa.setProgress(luzBaixa.getProgress()-1);
                break;
        }

    }

    private void setSucessoAoEnviarConfigTempo(){
        getActivity().runOnUiThread(()->{
            layoutConfigTempo.setBackgroundResource(R.drawable.layout_comando_enviado_sucesso);
        });
    }
    private void setErroAoEnviarConfigTempo(){
        getActivity().runOnUiThread(()->{
            layoutConfigTempo.setBackgroundResource(R.drawable.layout_comando_enviado_falha);
        });
    }
    private void setSucessoAoEnviarLuminosidadeBaixa(){
        getActivity().runOnUiThread(()->{
            layoutConfigLuminosidadeBaixa.setBackgroundResource(R.drawable.layout_comando_enviado_sucesso);
        });
    }
    private void setErroAoEnviarLuminosidadeBaixa(){
        getActivity().runOnUiThread(()->{
            layoutConfigLuminosidadeBaixa.setBackgroundResource(R.drawable.layout_comando_enviado_falha);
        });

    }
    private void setSucessoAoEnviarLuminosidadeAlta(){
        getActivity().runOnUiThread(()->{
            layoutConfigLuminosidadeAlta.setBackgroundResource(R.drawable.layout_comando_enviado_sucesso);
        });
    }
    private void setErroAoEnviarLuminosidadeAlta(){
        getActivity().runOnUiThread(()->{
            layoutConfigLuminosidadeAlta.setBackgroundResource(R.drawable.layout_comando_enviado_falha);
        });

    }

    private void enviandoComando(){
        getActivity().runOnUiThread(()->{
            salvarConfigTempo.setEnabled(false);
            salvarConfigLuminosidadeAlta.setEnabled(false);
            salvarConfigLuminosidadeBaixa.setEnabled(false);
            valorLuzBaixa.setEnabled(false);
            valorLuzAlta.setEnabled(false);
            luzBaixa.setEnabled(false);
            luzAlta.setEnabled(false);
            maisLuzBaixa.setEnabled(false);
            maisLuzAlta.setEnabled(false);
            menosLuzBaixa.setEnabled(false);
            menosLuzAlta.setEnabled(false);
        });
    }
    private void fimEnviandoComando(){
        getActivity().runOnUiThread(()->{
            salvarConfigTempo.setEnabled(true);
            salvarConfigLuminosidadeAlta.setEnabled(true);
            salvarConfigLuminosidadeBaixa.setEnabled(true);
            valorLuzAlta.setEnabled(true);
            valorLuzBaixa.setEnabled(true);
            luzBaixa.setEnabled(true);
            luzAlta.setEnabled(true);
            maisLuzBaixa.setEnabled(true);
            maisLuzAlta.setEnabled(true);
            menosLuzBaixa.setEnabled(true);
            menosLuzAlta.setEnabled(true);
        });
    }

    private void updateValoresLuzAlta(Integer valor){
        getActivity().runOnUiThread(()->{
            luzAlta.setProgress(valor);
            porcentagemLuzAlta.setText(String.format("%03d",valor));
        });
    }
    private void updateValoresLuzBaixa(Integer valor){
        getActivity().runOnUiThread(()->{
            luzBaixa.setProgress(valor);
            porcentagemLuzBaixa.setText(String.format("%03d",valor));
        });
    }


    /***
     *
     * SEEKBAR CALLBACKS
     *
     * */

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.seekBarConfigLuzAlta:
                luzBaixa.setEnabled(false);
                break;
            case R.id.seekBarConfigLuzBaixa:
                luzAlta.setEnabled(false);
                break;
        }
        enviaComandoIntensidade(progress,seekBar);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        luzBaixa.setEnabled(true);
        luzAlta.setEnabled(true);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        enviaComandoIntensidade(seekBar.getProgress(),seekBar);
    }


    /**
     * COMANDOS BLUETOOTH LE
     *
     *
     * */
    private void enviaComandoIntensidade(Integer valor,SeekBar seekBar){
        byte[] data = new byte[]{control.luminariaLugar.getEnd1().byteValue(),control.luminariaLugar.getEnd2().byteValue(),control.luminariaLugar.getEnd3().byteValue(),valor.byteValue()};
        if(control.deviceConected!=null){
            if(BleManager.getInstance().isConnected(control.deviceConected)){
                BleManager.getInstance().write(control.deviceConected, ConstBle.SERVICE_GENERICO_RGB, ConstBle.CHARACTERISTIC_GENERICO_RGB, data, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Byte intensidade = new Byte(justWrite[3]);
                        switch (seekBar.getId()){
                            case R.id.seekBarConfigLuzAlta:
                                updateValoresLuzAlta(intensidade.intValue());
                                break;
                            case R.id.seekBarConfigLuzBaixa:
                                updateValoresLuzBaixa(intensidade.intValue());
                                break;
                        }
                        // AVISO ESSA LINHA CAUSA QUE NA TELA ANTETRIOR VOLTE PARA O VALOR ANTERIOR DE INTENSIDADE POR CAUSA DE UMA CHAMADA PADRÃO DO SISTEMA
                        // control.intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_ATUAL] = new Byte(justWrite[3]).intValue();
                        fimEnviandoComando();
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {

                    }
                });

            }
        }

    }
    private void enviarComandoSalvarCOnfigTempoLuzBaixa(){
        int valorTempo = Integer.parseInt(valorLuzBaixa.getText().toString());
        byte [] data  = new byte[2];
        data[1] = (byte) valorTempo;
        data[0] = (byte) (valorTempo >> 8);
        if(control.deviceConected!=null){
            if(BleManager.getInstance().isConnected(control.deviceConected)){
                BleManager.getInstance().write(control.deviceConected, ConstBle.SERVICE_TIME, ConstBle.CHARACTERISTIC_GRAVAR_LUZ_BAIXA, data, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        setSucessoAoEnviarConfigTempo();
                        fimEnviandoComando();
                        control.intensidadeAtual[Const.POS_LUZ_BAIXA] = valorTempo;
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        setErroAoEnviarConfigTempo();
                        fimEnviandoComando();
                    }
                });
            }
        }

    }
    private void enviarComandoSalvarConfigTempoLuzAlta(){
        int valorTempo = Integer.parseInt(valorLuzAlta.getText().toString());
        byte [] data  = new byte[2];
        data[1] = (byte) valorTempo;
        data[0] = (byte) (valorTempo >> 8);
        if(control.deviceConected!=null){
            if(BleManager.getInstance().isConnected(control.deviceConected)){
                BleManager.getInstance().write(control.deviceConected, ConstBle.SERVICE_TIME, ConstBle.CHARACTERISTIC_GRAVAR_LUZ_ALTA, data, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        enviarComandoSalvarCOnfigTempoLuzBaixa();
                        control.intensidadeAtual[Const.POS_LUZ_ALTA] = valorTempo;
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        setErroAoEnviarConfigTempo();
                        fimEnviandoComando();
                    }
                });
            }
        }
    }
    private void enviarComandoSalvarConfigLuminosidadeAlta(){
        byte [] data = new byte[]{control.luminariaLugar.getEnd1().byteValue(),control.luminariaLugar.getEnd2().byteValue(),control.luminariaLugar.getEnd3().byteValue(),ConstBle.GRAVAR_LUZ_ALTA.byteValue()};
        if(control.deviceConected != null){
            if(BleManager.getInstance().isConnected(control.deviceConected)){
                BleManager.getInstance().write(control.deviceConected, ConstBle.SERVICE_GENERICO_RGB, ConstBle.CHARACTERISTIC_GENERICO_RGB, data, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        fimEnviandoComando();
                        setSucessoAoEnviarLuminosidadeAlta();
                        control.intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_ALTA] = luzAlta.getProgress();
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        fimEnviandoComando();
                        setErroAoEnviarLuminosidadeAlta();
                    }
                });
            }
        }
    }
    private void enviarComandoSalvarConfigLuminosidadeBaixa(){
        byte [] data = new byte[]{control.luminariaLugar.getEnd1().byteValue(),control.luminariaLugar.getEnd2().byteValue(),control.luminariaLugar.getEnd3().byteValue(),ConstBle.GRAVAR_LUZ_BAIXA.byteValue()};
        if(control.deviceConected != null){
            if(BleManager.getInstance().isConnected(control.deviceConected)){
                BleManager.getInstance().write(control.deviceConected, ConstBle.SERVICE_GENERICO_RGB, ConstBle.CHARACTERISTIC_GENERICO_RGB, data, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        fimEnviandoComando();
                        setSucessoAoEnviarLuminosidadeBaixa();
                        control.intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_BAIXA] = luzBaixa.getProgress();
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        fimEnviandoComando();
                       setErroAoEnviarLuminosidadeBaixa();
                    }
                });
            }
        }
    }

}
