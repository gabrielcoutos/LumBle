package br.com.gabrielcouto.ledtec.homepage;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.scan.BleScanRuleConfig;

import java.util.UUID;

import br.com.gabrielcouto.ledtec.R;
import br.com.gabrielcouto.ledtec.homepage.fragments.CadastroFragment;
import br.com.gabrielcouto.ledtec.homepage.fragments.Comunicador;
import br.com.gabrielcouto.ledtec.homepage.fragments.ConfiguracaoFragment;
import br.com.gabrielcouto.ledtec.homepage.fragments.LuminariasFragment;
import br.com.gabrielcouto.ledtec.homepage.fragments.SobreFragment;
import br.com.gabrielcouto.ledtec.model.entity.LuminariaLugar;
import br.com.gabrielcouto.ledtec.utils.ConstBle;
import br.com.gabrielcouto.ledtec.utils.Utils;


public class HomePage extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, Button.OnClickListener, Comunicador {
    public static int nivel =1; // controla o nivel , podendo ser 1,2 e maximo 3
    private View toolbar;
    public TextView toolbarTittle;
    public ImageButton toolbarBack;
    private ImageButton toolbarPlus;
    private BottomNavigationView navView;
    private LuminariaLugar luminariaLugar;
    private String[] controleToolbar = new String[3]; //  faz o controle dos nomes do titulo da toolbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        checkPermission();
        checkBleOn();
        setupBleManager();
        initReferences();
    }

    /**
     * Inicializa as referencias e seta cas funcionalidades de click
     * */
    private void initReferences(){
        toolbar = findViewById(R.id.toolbarHomePage);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this);
        toolbarTittle = toolbar.findViewById(R.id.toolbarTittle);
        toolbarBack = toolbar.findViewById(R.id.btnBackToolbar);
        toolbarPlus = toolbar.findViewById(R.id.addBtnToolbar);
        toolbarPlus.setOnClickListener(this);
        toolbarBack.setOnClickListener(this);
        controleToolbar[0]= "LUMINÁRIAS";
        navView.setSelectedItemId( R.id.luminarias);
    }

    /**
     * Inicializa o Ble para uso em todas as outras telas
     * */
    private void setupBleManager(){
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance().enableLog(true).setReConnectCount(3,250).setOperateTimeout(3000);
        BleScanRuleConfig bleScanRuleConfig = new BleScanRuleConfig.Builder()
                .setScanTimeOut(2000)
                .setDeviceName(true,"LEDTEC","*LEDTEC")
                .setAutoConnect(false)
                .build();
        BleManager.getInstance().initScanRule(bleScanRuleConfig);
    }


    private void hideBackBtn(){
        toolbarBack.setVisibility(View.GONE);
    }
    private void showBackBtn(){
        toolbarBack.setVisibility(View.VISIBLE);
    }
    private void hidePlusBtn(){
        toolbarPlus.setVisibility(View.GONE);
    }
    private void showPlusBtn(){
        toolbarPlus.setVisibility(View.VISIBLE);
    }

    /**
     * Carrega as fragments
     * */
    private boolean loadFragment(Fragment fragment, boolean replace){

        if(fragment !=null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_up,R.anim.exit_to_up,R.anim.enter_from_up,R.anim.exit_to_up);
            transaction.addToBackStack(null);
            if(replace)
                transaction.replace(R.id.fragmentConteiner,fragment,fragment.getClass().getName()).commit();
            else
                transaction.replace(R.id.fragmentConteiner,fragment,fragment.getClass().getName()).commit();
            return true;
        }
        return false;
    }


    /**
     * Listener de click da barra de navegação
     * */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        switch (menuItem.getItemId()) {
            case R.id.configuracoes:
                String tittle = getString(R.string.title_configuracoes);
                toolbarTittle.setText(tittle.toUpperCase());
                hideBackBtn();
                hidePlusBtn();
                BleManager.getInstance().disconnectAllDevice();
                fragment = new ConfiguracaoFragment();
                nivel =1;
                break;
            case R.id.luminarias:
                tittle = getString(R.string.title_luminaria);
                if(nivel == 1){
                    hideBackBtn();
                }
                showPlusBtn();
                toolbarTittle.setText(tittle.toUpperCase());
                fragment = new LuminariasFragment();
                break;
            case R.id.sobre:
                tittle = getString(R.string.title_sobre);
                hideBackBtn();
                hidePlusBtn();
                toolbarTittle.setText(tittle.toUpperCase());
                fragment = new SobreFragment();
                BleManager.getInstance().disconnectAllDevice();
                nivel =1;
                break;
        }
        return loadFragment(fragment,true);
    }


    /**
     * Gerencia os clicks
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //botão de cadastro quando está com a lista vazia
            case R.id.btnCadastrarLumLocal:
            case R.id.addBtnToolbar:
                toolbarTittle.setText("Cadastrar");
                Fragment fragment = new CadastroFragment();
                hidePlusBtn();
                // adiciona como paramêtro o pai de determinado local ou luminária
                Bundle bundle = new Bundle();
                bundle.putSerializable("father",luminariaLugar);
                fragment.setArguments(bundle);
                loadFragment(fragment,false);
                showBackBtn();
                break;
            case R.id.btnBackToolbar:
                // se for tela de cadastro não se deve decrementar o nível, para voltar exatamente ao ponto em que estava anteriormente
                CadastroFragment fragment1 = (CadastroFragment) getSupportFragmentManager().findFragmentByTag(CadastroFragment.class.getName());
                if(fragment1!= null && fragment1.isVisible()){
                    getSupportFragmentManager().popBackStack();
                    getSupportFragmentManager().beginTransaction().remove(fragment1).commit();
                    ajustarToolbar();
                }else{
                    if(nivel >1){
                        nivel--;
                        getSupportFragmentManager().popBackStack();
                    }
                    ajustarToolbar();
                }

                break;
        }
    }

    /**
     * Evento que é chamado quando ocorre um click em um local, recebendo o local que foi selecionado.
     * passando o local clicado como paramêtro para saber quem é o pai do local que está sendo mostrado
     * */
    @Override
    public void event(LuminariaLugar father) {
        nivel++;
        luminariaLugar = father;
        controleToolbar[nivel-1] = luminariaLugar.getNome();
        Bundle bundle = new Bundle();
        bundle.putSerializable("father",father);
        LuminariasFragment fragment = new LuminariasFragment();
        fragment.setArguments(bundle);
        if(father != null){
            toolbarTittle.setText(father.getNome());
            showBackBtn();
        }
        loadFragment(fragment,false);
        ajustarToolbar();
    }

    /**
     * Ajusta o nome que fica na toolbar para mostrar em qual local o ususário está.
     * */
    private void ajustarToolbar(){
        runOnUiThread(()->{
            switch (nivel){
                case 1:
                    toolbarTittle.setText(controleToolbar[0]);
                    luminariaLugar= null;
                    hideBackBtn();
                    showPlusBtn();
                    break;
                case 2:
                    toolbarTittle.setText(controleToolbar[nivel -1]);
                    showBackBtn();
                    showPlusBtn();
                    break;
                case 3:
                    toolbarTittle.setText(controleToolbar[nivel -1]);
                    showBackBtn();
                    showPlusBtn();
                    break;

            }
        });
    }

    private void checkBleOn(){
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter != null){
            if(!btAdapter.isEnabled()){
                Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enable,Utils.REQUEST_ENABLE_BT);
            }
        }
    }

    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(HomePage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestFineLocation();
        }

    }

    private void requestFineLocation(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Permissão necessária")
                    .setMessage("Permissão necessária para realizar o scan de dispositivos próximos")
                    .setPositiveButton("Certo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(HomePage.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Utils.REQUEST_LOCATION_FINE);
                        }
                    })
                    .setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();

        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},Utils.REQUEST_LOCATION_FINE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == Utils.REQUEST_LOCATION_FINE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                Toast.makeText(HomePage.this,"O app não funcionará corretamente sem a permissão",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == Utils.REQUEST_ENABLE_BT){
            if(resultCode != RESULT_OK){
                checkBleOn();
            }
        }
    }

    /**
     * IMPORTANTE sempre usar a função do click do botão de voltar sobreescrevendo o back pressed
     * Se isso não for feito vai causar problemas no app.
     * */

    @Override
    public void onBackPressed() {
        toolbarBack.callOnClick();
    }
}
