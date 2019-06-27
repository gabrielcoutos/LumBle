package br.com.gabrielcouto.ledtec.controle;

import android.bluetooth.BluetoothGatt;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import br.com.gabrielcouto.ledtec.R;
import br.com.gabrielcouto.ledtec.controle.fragments.ConfigLuminariaFragment;
import br.com.gabrielcouto.ledtec.controle.fragments.ControleLuminariaFragment;
import br.com.gabrielcouto.ledtec.model.entity.LuminariaLugar;
import br.com.gabrielcouto.ledtec.utils.Const;
import br.com.gabrielcouto.ledtec.utils.ConstBle;

public class ControleLuminaria extends AppCompatActivity implements View.OnClickListener {

    private ConstraintLayout progressConect;
    private View toolbarControleLum;
    public LuminariaLugar luminariaLugar;
    public BleDevice deviceConected;
    public Integer[] intensidadeAtual = new Integer[9];
    public String versao;
    private Button reconect;
    private ImageButton toolbarback,configBtn;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controle_luminaria);
        initInstances();
        getParameters();
        startConect();
    }

    private void initInstances() {
        progressConect = findViewById(R.id.idProgressConectControleLum);
        toolbarControleLum = findViewById(R.id.idToolbarControleLum);
        reconect = findViewById(R.id.btnReconnectControleLum);
        reconect.setOnClickListener(this);
        title = toolbarControleLum.findViewById(R.id.toolbarTittle);
        toolbarback = toolbarControleLum.findViewById(R.id.btnBackToolbar);
        configBtn = toolbarControleLum.findViewById(R.id.addBtnToolbar);
        configBtn.setBackgroundResource(R.drawable.ic_settings_white_30dp);
        configBtn.setOnClickListener(this);
        toolbarback.setOnClickListener(this);
    }

    private void getParameters() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            luminariaLugar = (LuminariaLugar) bundle.getSerializable("conectLum");
            deviceConected = (BleDevice) bundle.getParcelable("conectDevice");
            title.setText(luminariaLugar.getNome());
        }
    }

    private void startConect() {
        if (deviceConected != null) {
            BleManager.getInstance().connect(deviceConected,gattCallback);
        }else{
            // AQUI ENTRA A REDE MESH
        }
    }


    private void showProgress() {
        progressConect.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressConect.setVisibility(View.GONE);
    }

    private void hideReconect() {
        reconect.setVisibility(View.GONE);
    }

    private void showReconect() {
        reconect.setVisibility(View.VISIBLE);
    }

    private void showConfig(){configBtn.setVisibility(View.VISIBLE);}

    private void hideConfig(){configBtn.setVisibility(View.GONE);}

    private void verificaSensor(){
        if(intensidadeAtual[Const.PROTOCOLO_TIPO_LUM] == Const.LUMINARIA_SENSOR_PYRO){
            showConfig();
        }else{
            hideConfig();
        }
    }

    private BleGattCallback gattCallback = new BleGattCallback() {
        @Override
        public void onStartConnect() {
            hideReconect();
            hideConfig();

        }

        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {
            deviceConected = bleDevice;
            showReconect();
            hideProgress();
            hideConfig();
            removeFragment();
        }

        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
            deviceConected = bleDevice;
            getVersao();
        }

        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
            deviceConected = device;
            showReconect();
            hideProgress();
            hideConfig();
            removeFragment();
        }
    };

    @Override
    public void onBackPressed() {
        toolbarback.callOnClick();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnReconnectControleLum:
                showProgress();
                hideReconect();
                BleManager.getInstance().connect(luminariaLugar.getMac(), gattCallback);
                break;
            case R.id.btnBackToolbar:
                ConfigLuminariaFragment configLuminariaFragment = (ConfigLuminariaFragment) getSupportFragmentManager().findFragmentByTag(ConfigLuminariaFragment.class.getName());
                if(configLuminariaFragment !=null && configLuminariaFragment.isVisible()){
                   getSupportFragmentManager().popBackStack();
                   showConfig();
                }else {
                    BleManager.getInstance().disconnectAllDevice();
                    finish();
                }
                break;
            case R.id.addBtnToolbar:
                ConfigLuminariaFragment fragment = new ConfigLuminariaFragment();
                hideConfig();
                loadFragment(fragment);
                break;
        }

    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_up, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_up);
            transaction.addToBackStack(null);
            transaction.replace(R.id.fragmentConteinerControleLum, fragment, fragment.getClass().getName()).commit();
            return true;
        }
        return false;
    }

    private void removeFragment(){
        getSupportFragmentManager().popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);


    }

    private void getVersao(){
        BleManager.getInstance().read(deviceConected, ConstBle.SERVICE_GERAL, ConstBle.CHARACTERISTIC_LER_VERSAO, new BleReadCallback() {
            @Override
            public void onReadSuccess(byte[] data) {
                versao = new String(data);
                getInformacoes();
            }

            @Override
            public void onReadFailure(BleException exception) {
                BleManager.getInstance().disconnectAllDevice();
            }
        });
    }
    private void  getInformacoes(){
        BleManager.getInstance().read(deviceConected, ConstBle.SERVICE_GERAL, ConstBle.CHARACTERISTIC_LER_INFORMACOES, new BleReadCallback() {
            @Override
            public void onReadSuccess(byte[] data) {
                intensidadeAtual[Const.PROTOCOLO_TIPO_LUM] = (Byte.valueOf(data[Const.PROTOCOLO_TIPO_LUM])).intValue();
                intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_ATUAL] = (Byte.valueOf(data[Const.PROTOCOLO_LUMINOSIDADE_ATUAL])).intValue();
                intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_ALTA] = (Byte.valueOf(data[Const.PROTOCOLO_LUMINOSIDADE_ALTA])).intValue();
                intensidadeAtual[Const.PROTOCOLO_LUMINOSIDADE_BAIXA] = (Byte.valueOf(data[Const.PROTOCOLO_LUMINOSIDADE_BAIXA])).intValue();
                int tempoAlta = (Byte.valueOf(data[Const.PROTOCOLO_TEMPO_LUZ_ALTA1])).intValue() << 8;
                tempoAlta |= (Byte.valueOf(data[Const.PROTOCOLO_TEMPO_LUZ_ALTA2])).intValue();
                int tempoBaixa = (Byte.valueOf(data[Const.PROTOCOLO_TEMPO_LUZ_BAIXA1])).intValue() << 8;
                tempoBaixa |= (Byte.valueOf(data[Const.PROTOCOLO_TEMPO_LUZ_BAIXA2])).intValue();
                intensidadeAtual[Const.POS_LUZ_ALTA] = tempoAlta;
                intensidadeAtual[Const.POS_LUZ_BAIXA] = tempoBaixa;
                intensidadeAtual[Const.POS_MANTER_LUZ_BAIXA] = (Byte.valueOf(data[Const.PROTOCOLO_MANTER_LUZ_BAIXA])).intValue();
                hideProgress();
                hideReconect();
                verificaSensor();
                ControleLuminariaFragment fragment = new ControleLuminariaFragment();
                loadFragment(fragment);
            }

            @Override
            public void onReadFailure(BleException exception) {
                BleManager.getInstance().disconnectAllDevice();
            }
        });

    }





}
