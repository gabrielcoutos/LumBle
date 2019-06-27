package br.com.gabrielcouto.ledtec.controle.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;

import br.com.gabrielcouto.ledtec.R;
import br.com.gabrielcouto.ledtec.controle.ControleLuminaria;
import br.com.gabrielcouto.ledtec.utils.Const;
import br.com.gabrielcouto.ledtec.utils.ConstBle;

public class ControleLuminariaFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private View rootView;
    private Button mais,menos;
    private ImageView ligarDesligar;
    private SeekBar controleSeekBar;
    private TextView porcentagem,versaoTextView,luminosidadeAtual;
    private boolean ligadoDesligado = false;
    private ControleLuminaria control;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_controle_luminaria,null);
        initInstances();
        setInitValores();
        return rootView;
    }


    private void initInstances(){
        menos = rootView.findViewById(R.id.btnLessControleLum);
        menos.setOnClickListener(this);
        mais = rootView.findViewById(R.id.btnPlusControleLum);
        mais.setOnClickListener(this);
        ligarDesligar = rootView.findViewById(R.id.btnLigarDesligarControleLum);
        ligarDesligar.setOnClickListener(this);
        controleSeekBar = rootView.findViewById(R.id.seekBarControleLum);
        porcentagem = rootView.findViewById(R.id.txtValorControleLum);
        versaoTextView = rootView.findViewById(R.id.txtVersaoControleLum);
        luminosidadeAtual = rootView.findViewById(R.id.txtLuminosidadeControleLum);
        control = (ControleLuminaria) getActivity();
        versaoTextView.setText(control.versao);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        controleSeekBar = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLessControleLum:
                if(controleSeekBar.getProgress()>0)
                    controleSeekBar.setProgress(controleSeekBar.getProgress()-1);
                break;
            case R.id.btnPlusControleLum:
                if(controleSeekBar.getProgress()<100)
                    controleSeekBar.setProgress(controleSeekBar.getProgress()+1);
                break;
            case R.id.btnLigarDesligarControleLum:
                changeLigarDesligar();
                break;
        }
    }

    private void changeLigarDesligar(){
        ligadoDesligado = !ligadoDesligado;
        if(ligadoDesligado) {
            enviarComandoLigarDesligar(100);
        }
        else {
            enviarComandoLigarDesligar(0);
        }
    }

    private void enviarComando(Integer valor){
        byte [] data = new byte[]{control.luminariaLugar.getEnd1().byteValue(),control.luminariaLugar.getEnd2().byteValue(),control.luminariaLugar.getEnd3().byteValue(),valor.byteValue()};
        if(control.deviceConected != null){
            if(BleManager.getInstance().isConnected(control.deviceConected)){
                BleManager.getInstance().write(control.deviceConected, ConstBle.SERVICE_GENERICO_RGB, ConstBle.CHARACTERISTIC_GENERICO_RGB, data, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        if(justWrite[3]>0)
                            ligadoDesligado=true;
                        else
                            ligadoDesligado= false;
                        setImagem();
                        control.intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_ATUAL] = new Byte(justWrite[3]).intValue();
                    }
                    @Override
                    public void onWriteFailure(BleException exception) {

                    }
                });
            }
        }
    }

    private void enviarComandoLigarDesligar(Integer valor){
        byte [] data = new byte[]{control.luminariaLugar.getEnd1().byteValue(),control.luminariaLugar.getEnd2().byteValue(),control.luminariaLugar.getEnd3().byteValue(),valor.byteValue()};
        if(control.deviceConected != null){
            if(BleManager.getInstance().isConnected(control.deviceConected)){
                BleManager.getInstance().write(control.deviceConected, ConstBle.SERVICE_GENERICO_RGB, ConstBle.CHARACTERISTIC_GENERICO_RGB, data, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        setImagem();
                        setTextProgress(String.valueOf(justWrite[3]));
                        controleSeekBar.setProgress(new Byte(justWrite[3]).intValue());

                    }
                    @Override
                    public void onWriteFailure(BleException exception) {
                        Toast.makeText(getActivity(),"Erro ao enviar comando",Toast.LENGTH_SHORT).show();
                        ligadoDesligado = !ligadoDesligado;
                        setImagem();
                    }
                });
            }
        }
    }

    private void setImagem(){
        getActivity().runOnUiThread(()->{
            if(ligadoDesligado) {
                ligarDesligar.setImageResource(R.drawable.btn_ligado);
            }
            else {
                ligarDesligar.setImageResource(R.drawable.btn_lum_desligada);
            }
        });
    }

    private void setTextProgress(String text){
        int porcent = Integer.parseInt(text);
        getActivity().runOnUiThread(()->{
            porcentagem.setText(String.format("%03d",porcent));
        });
    }

    private void setInitValores(){
        if(control.intensidadeAtual[1]>0){
            ligadoDesligado = true;
        }else{
            ligadoDesligado = false;
        }
        setImagem();
        controleSeekBar.setProgress(control.intensidadeAtual[1]);
        setTextProgress(control.intensidadeAtual[1].toString());
        if(control.intensidadeAtual[Const.PROTOCOLO_TIPO_LUM] == Const.LUMINARIA_SENSOR_LUMINOSIDADE){
            initNotificacao();
        }
        controleSeekBar.setOnSeekBarChangeListener(this);
    }

    public void initNotificacao(){
        if(control.deviceConected!=null){
            if(BleManager.getInstance().isConnected(control.deviceConected)){
                BleManager.getInstance().notify(control.deviceConected, ConstBle.SERVICE_GERAL, ConstBle.NOTIFICACAO_LUMINOSIDADE, new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {

                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String luminosidade  = new String(data);
                        setLuminosidadeAtual(luminosidade);

                    }
                });
            }
        }

    }

    private void setLuminosidadeAtual(String text){
        getActivity().runOnUiThread(()->{
            luminosidadeAtual.setText("LUMINOSIDADE ATUAL: "+text);
        });

    }



    /**
     * SEEKBAR
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(progress>0){
            ligadoDesligado = true;
        }else{
            ligadoDesligado = false;
        }
        setTextProgress(String.valueOf(seekBar.getProgress()));
        enviarComando(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(seekBar.getProgress() ==0){
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            enviarComando(seekBar.getProgress());
        }

    }
    /***/

}
