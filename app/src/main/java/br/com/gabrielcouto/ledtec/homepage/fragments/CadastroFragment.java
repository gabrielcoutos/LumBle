package br.com.gabrielcouto.ledtec.homepage.fragments;

import android.bluetooth.BluetoothGatt;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.gabrielcouto.ledtec.R;
import br.com.gabrielcouto.ledtec.adapter.ScanBleAdapter;
import br.com.gabrielcouto.ledtec.homepage.HomePage;
import br.com.gabrielcouto.ledtec.model.dao.DatabaseHelper;
import br.com.gabrielcouto.ledtec.model.dao.LuminariaLugarDaoImpl;
import br.com.gabrielcouto.ledtec.model.entity.LuminariaLugar;
import br.com.gabrielcouto.ledtec.utils.Const;
import br.com.gabrielcouto.ledtec.utils.ConstBle;

public class CadastroFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,ScanBleAdapter.OnBleDeviceListener {
    private String icone;
    private TextView progressText,textEmptyLumCadastro;
    private ProgressBar conectProgress,progressSalvarLocal,progressBarCadastrarLuminaria;
    private View rootView;
    private EditText name,nameLuminaria;
    private CheckBox local,lum;
    private ImageButton escritorio,garagem,casa,quarto,tv,banheiro;
    private Button salvar,cadastrarLum;
    private DatabaseHelper dh;
    private LuminariaLugar father,lumCadastrada;
    private ConstraintLayout layoutCadastrarLocal, layoutCadastrarLumnaria;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView scanList;
    private ScanBleAdapter adapter;
    private List<BleDevice> bleDeviceList = new ArrayList<>();
    private BleDevice selecionadoBle;
    private Boolean isCadastrandoLum = false;
    private List<String> devicesBd = new ArrayList<>();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle!=null) {
            father = (LuminariaLugar) bundle.getSerializable("father");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_cadastro,null);
        initInstances();
        initDevicesBd();
        return rootView;
    }
    /**
     *
     * SETUP inicial
     * */
    private void initInstances(){
        name = rootView.findViewById(R.id.editNomeLum);
        name.addTextChangedListener(nameListener);
        local = rootView.findViewById(R.id.checkBoxLocalCadastro);
        local.setOnCheckedChangeListener(this);
        lum = rootView.findViewById(R.id.checkBoxLumCadastro);
        lum.setOnCheckedChangeListener(this);
        escritorio = rootView.findViewById(R.id.imgCadastroEscritorio);
        escritorio.setOnClickListener(this);
        garagem = rootView.findViewById(R.id.imgCadastroGaragem);
        garagem.setOnClickListener(this);
        casa = rootView.findViewById(R.id.imgCadastroCasa);
        casa.setOnClickListener(this);
        quarto = rootView.findViewById(R.id.imgCadastroQuarto);
        quarto.setOnClickListener(this);
        tv = rootView.findViewById(R.id.imgCadastroTV);
        tv.setOnClickListener(this);
        banheiro = rootView.findViewById(R.id.imgCadastroBanheiro);
        banheiro.setOnClickListener(this);
        salvar = rootView.findViewById(R.id.btnCadastroSalvar);
        salvar.setOnClickListener(this);
        escritorio.callOnClick(); // set inicial icone
        layoutCadastrarLocal = rootView.findViewById(R.id.layoutCadastrarLocal);
        layoutCadastrarLumnaria = rootView.findViewById(R.id.layoutCadastrarLuminaria);
        mSwipeRefreshLayout = rootView.findViewById(R.id.refreshLayout);
        scanList = rootView.findViewById(R.id.recycleViewScanCadastro);
        progressText = rootView.findViewById(R.id.textProgressCadastro);
        conectProgress = rootView.findViewById(R.id.idProgressConectCadastro);
        progressSalvarLocal = rootView.findViewById(R.id.progressBtnSalve);
        cadastrarLum = rootView.findViewById(R.id.btnCadastrarLuminaria);
        cadastrarLum.setOnClickListener(this);
        nameLuminaria = rootView.findViewById(R.id.idTxtNomeLuminariaCadastro);
        nameLuminaria.addTextChangedListener(luminariaNameListener);
        progressBarCadastrarLuminaria = rootView.findViewById(R.id.progressBarCadastrarLuminaria);
        textEmptyLumCadastro = rootView.findViewById(R.id.TextEmptyLumCadastro);
        setupRecycleView();
        showLocalCadastro();
        if(HomePage.nivel ==3){ // nivel 3 não se pode cadastrar locais
            local.setChecked(false);
            local.setEnabled(false);
            lum.setChecked(true);
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BleManager.getInstance().scan(bleScanCallback);
                textEmptyLumCadastro.setVisibility(View.GONE);
            }
        });
    }
    private void  setupRecycleView(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        scanList.setLayoutManager(linearLayoutManager);
        adapter = new ScanBleAdapter(bleDeviceList,this::onBleDeviceClick);
        scanList.setAdapter(adapter);
    }

    /** verifica os dispositivos(luminárias) cadastrados no app para nao permitir duplicar
     *
     * */
    private void initDevicesBd(){
        new Thread(()->{
            dh = DatabaseHelper.getInstance(getContext());
            LuminariaLugarDaoImpl dao = null;
            try {
                dao = new LuminariaLugarDaoImpl(dh.getConnectionSource());
                QueryBuilder<LuminariaLugar,Integer> qb = dao.queryBuilder();
                qb.where().isNotNull("mac");
                List<LuminariaLugar> allLum = qb.query();
                if(allLum !=null){
                    for(LuminariaLugar mac : allLum){
                        devicesBd.add(mac.getMac());
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }).start();
    }

    /**
     * Prepara uma luminária para ser cadastrada, lembrando que ela de fato so vai ser cadastrada no momento em que responder o comando de escrita
     * */
    private void cadastrarLuminaria(){
        new Thread(()->{
            dh = DatabaseHelper.getInstance(getContext());
            LuminariaLugarDaoImpl dao = null;
            try {
                dao = new LuminariaLugarDaoImpl(dh.getConnectionSource());
                lumCadastrada = new LuminariaLugar();
                QueryBuilder<LuminariaLugar,Integer> qb = dao.queryBuilder();
                qb.where().eq("level",HomePage.nivel);
                if(father != null){
                    qb.where().eq("level",HomePage.nivel).and().eq("lumPlace_id",father.getId());
                }
                qb.orderBy("idLevel",false);
                List<LuminariaLugar> list = qb.query();
                if(selecionadoBle !=null){
                    lumCadastrada.setMac(selecionadoBle.getMac());
                    lumCadastrada.setNome(nameLuminaria.getText().toString());

                }
                lumCadastrada.setIcone(Const.ICONE_LUM);
                lumCadastrada.setLevel(HomePage.nivel);
                lumCadastrada.setFather(father);
                LuminariaLugar grandFather = null;
                if(father != null) {
                    if (father.getFather() != null) {
                        grandFather = dao.queryForId(father.getFather().getId());
                    }
                }
                if(list.isEmpty() || list == null){
                    lumCadastrada.setIdLevel(1);
                }else{
                    int level = list.get(0).getIdLevel();
                    level++;
                    lumCadastrada.setIdLevel(level);
                }
                switch (HomePage.nivel){
                    case 1:
                        lumCadastrada.setEnd1(lumCadastrada.getIdLevel());
                        lumCadastrada.setEnd2(1);
                        lumCadastrada.setEnd3(1);
                        break;
                    case 2:
                        lumCadastrada.setEnd1(father.getIdLevel());
                        lumCadastrada.setEnd2(lumCadastrada.getIdLevel());
                        lumCadastrada.setEnd3(1);
                        break;
                    case 3:
                        lumCadastrada.setEnd1(grandFather.getIdLevel());
                        lumCadastrada.setEnd2(father.getIdLevel());
                        lumCadastrada.setEnd3(lumCadastrada.getIdLevel());
                        break;
                }
                if(selecionadoBle!= null){
                    /*
                    * protocolo no formato
                    * endereço | endereço 2| endereço 3 | comando de gravação
                    * */
                    byte[] data= {lumCadastrada.getEnd1().byteValue(),lumCadastrada.getEnd2().byteValue(),lumCadastrada.getEnd3().byteValue(),ConstBle.GRAVAR_ENDERECO.byteValue()};
                    BleManager.getInstance().write(selecionadoBle, ConstBle.SERVICE_GENERICO_RGB,ConstBle.CHARACTERISTIC_GENERICO_RGB,data,writeCallback); // comando necessário para cadastar
                }else{
                    hideProgressSalvarLuminaria();
                    Toast.makeText(getActivity(),"Erro ao cadastrar",Toast.LENGTH_SHORT).show();
                }

            } catch (SQLException e) {

            }
        }).start();

    }

    /**
     * comando chamado após a resposta da gravação
     * */
    private void saveLuminariaDB(){
        new Thread(()->{
            dh = DatabaseHelper.getInstance(getContext());
            LuminariaLugarDaoImpl dao = null;
            try {
                dao = new LuminariaLugarDaoImpl(dh.getConnectionSource());
                if(lumCadastrada!=null) {
                    int sucesso= dao.create(lumCadastrada);
                    if(sucesso !=-1){
                        BleManager.getInstance().disconnectAllDevice();
                        selecionadoBle= null;
                        lumCadastrada = null;
                        LuminariasFragment fragment = (LuminariasFragment) getActivity().getSupportFragmentManager().findFragmentByTag(LuminariasFragment.class.getName());
                        fragment.loadListLumPlace();
                        HomePage home = (HomePage) getActivity();
                        home.toolbarBack.callOnClick();
                    }else{
                        hideProgressSalvarLuminaria();
                        Toast.makeText(getActivity(),"Algo deu errado!",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getActivity(), "Algo deu errado!", Toast.LENGTH_SHORT).show();
                    hideProgressSalvarLuminaria();
                }
            } catch (SQLException e) {
                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }).start();
    }


    /**
     * Salva efetivamente um local (antes era junto a luminária e local , mas agora somente salva um local, para salvar um luminária use o metodo cadastrarLuminaria())
     * */
    private void salvarLumOrLugar() {
        new Thread(() -> {
            dh = DatabaseHelper.getInstance(getContext());
            try {
                LuminariaLugarDaoImpl dao = new LuminariaLugarDaoImpl(dh.getConnectionSource());
                LuminariaLugar lum = new LuminariaLugar();
                QueryBuilder<LuminariaLugar,Integer> qb = dao.queryBuilder();
                qb.where().eq("level",HomePage.nivel);
                if(father != null){
                    qb.where().eq("level",HomePage.nivel).and().eq("lumPlace_id",father.getId());
                }
                qb.orderBy("idLevel",false);
                List<LuminariaLugar> list = qb.query();
                if(local.isChecked()) {
                    lum.setNome(name.getText().toString());
                    lum.setIcone(icone);
                }
                lum.setLevel(HomePage.nivel);
                lum.setFather(father);
                if(list.isEmpty() || list == null){
                    lum.setIdLevel(1);
                }else{
                    int level = list.get(0).getIdLevel();
                    level++;
                    lum.setIdLevel(level);
                }
                switch (HomePage.nivel){
                    case 1:
                        lum.setEnd1(lum.getIdLevel());
                        lum.setEnd2(1);
                        lum.setEnd3(1);
                        break;
                    case 2:
                        lum.setEnd1(father.getIdLevel());
                        lum.setEnd2(lum.getIdLevel());
                        lum.setEnd3(1);
                        break;
                    case 3:
                        lum.setEnd1(father.getFather().getIdLevel());
                        lum.setEnd2(father.getIdLevel());
                        lum.setEnd3(lum.getIdLevel());
                        break;
                }
                int sucesso = -1;
                if(local.isChecked())
                    sucesso = dao.create(lum);
                if (sucesso != -1){
                    if(local.isChecked()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LuminariasFragment fragment = (LuminariasFragment) getActivity().getSupportFragmentManager().findFragmentByTag(LuminariasFragment.class.getName());
                                fragment.loadListLumPlace();
                                HomePage home = (HomePage) getActivity();
                                home.toolbarBack.callOnClick();
                            }
                        });
                    }
                }else{
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressSalvarLocal();
                            Toast.makeText(getActivity(),"Algo deu errado",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
    *
    * Verificadores
    *
    * */
    private void checkEnableButton(){
        if(verifyIcone() && verifyName()){
            salvar.setEnabled(true);
        }else{
            salvar.setEnabled(false);
        }

    }

    private void checkEnableButtonCadastrarLuminaria(){
        if(verifyNameLuminaria() && verifySelectedLuminaria())
            cadastrarLum.setEnabled(true);
        else
            cadastrarLum.setEnabled(false);
    }

    private boolean verifySelectedLuminaria(){
        boolean verify = false;
        if(selecionadoBle!= null){
            verify = true;
        }
        return verify;
    }
    private boolean verifyNameLuminaria(){
        boolean verify = false;
        if(nameLuminaria!= null && !nameLuminaria.getText().toString().isEmpty()){
            verify = true;
        }
        return verify;
    }

    private boolean verifyIcone(){
        boolean verify = false;
        if(icone !=null && !icone.isEmpty()){
            verify = true;
        }
        return verify;
    }

    private boolean verifyName(){
        boolean verify = false;
        if(name != null && !name.getText().toString().isEmpty()){
            verify = true;
        }
        return verify;
    }

    /**
    *
    * Listeners
    *
    * */
    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.btnCadastroSalvar) {
            setAllImageGrey();
        }
        switch(v.getId()){
            case R.id.imgCadastroEscritorio:
                escritorio.setImageResource(R.drawable.escritorio_black);
                icone = Const.ICONE_ESCRITORIO;
                break;
            case R.id.imgCadastroGaragem:
                garagem.setImageResource(R.drawable.garagem_black);
                icone = Const.ICONE_GARAGEM;
                break;
            case R.id.imgCadastroCasa:
                casa.setImageResource(R.drawable.house_black);
                icone = Const.ICONE_CASA;
                break;
            case R.id.imgCadastroQuarto:
                quarto.setImageResource(R.drawable.quarto_black);
                icone = Const.ICONE_QUARTO;
                break;
            case R.id.imgCadastroTV:
                tv.setImageResource(R.drawable.tv_black);
                icone = Const.ICONE_TV;
                break;
            case R.id.imgCadastroBanheiro:
                banheiro.setImageResource(R.drawable.banheiro_black);
                icone = Const.ICONE_BANHEIRO;
                break;
            case R.id.btnCadastroSalvar:
                showProgressSalvarLocal();
                salvarLumOrLugar();
                break;
            case R.id.btnCadastrarLuminaria:
                showProgressSalvarLuminaria();
                cadastrarLuminaria();
                break;
        }
        checkEnableButton();
    }

    /*Verifica apenas se o texto não está vazio para liberar o botão*/
    private TextWatcher nameListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkEnableButton();

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher luminariaNameListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkEnableButtonCadastrarLuminaria();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.checkBoxLocalCadastro:
                if(HomePage.nivel ==3 && isChecked) {
                    lum.setChecked(true);
                    local.setChecked(false);
                    Toast.makeText(getActivity(),"Não é possível cadastrar locais no nível 3",Toast.LENGTH_SHORT).show();
                }else{
                    if (isChecked && lum.isChecked()) {
                        lum.setChecked(false);
                    }
                    if(isChecked){
                        hideProgress();
                        showLocalCadastro();
                    }
                }
                break;
            case R.id.checkBoxLumCadastro:
                if(isChecked && local.isChecked()){
                    local.setChecked(false);
                }
                if(isChecked){
                    showLuminariasCadastro();
                    BleManager.getInstance().scan(bleScanCallback);
                }else{
                    BleManager.getInstance().disconnectAllDevice();
                    BleManager.getInstance().cancelScan();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                break;
        }
    }

    /**
    *
    * Metodos para mostrar/esconder objetos da tela
    *
    * */
    private void setAllImageGrey(){
        escritorio.setImageResource(R.drawable.escritorio_grey);
        garagem.setImageResource(R.drawable.garagem_grey);
        casa.setImageResource(R.drawable.house_grey);
        quarto.setImageResource(R.drawable.quarto_grey);
        tv.setImageResource(R.drawable.tv_grey);
        banheiro.setImageResource(R.drawable.banheiro_grey);
    }
    private void setupNameDefault(String msg){
        getActivity().runOnUiThread(()->{
            if(nameLuminaria != null && nameLuminaria.getText().toString().isEmpty()){
                nameLuminaria.setText(msg);
            }
            if(msg.isEmpty()){
                nameLuminaria.setText(msg);
            }
        });
    }

    private void setDisableIcone(){
        escritorio.setEnabled(false);
        garagem.setEnabled(false);
        casa.setEnabled(false);
        quarto.setEnabled(false);
        tv.setEnabled(false);
        banheiro.setEnabled(false);
    }
    private void setEnableIcone(){
        escritorio.setEnabled(true);
        garagem.setEnabled(true);
        casa.setEnabled(true);
        quarto.setEnabled(true);
        tv.setEnabled(true);
        banheiro.setEnabled(true);
    }
    private  void showLocalCadastro(){
        ScaleAnimation animation = new ScaleAnimation(1.0F,1.0F,0F,1.0F,1F,1F);
        animation.setDuration(1000);
        layoutCadastrarLumnaria.setVisibility(View.GONE);
        layoutCadastrarLocal.startAnimation(animation);
        layoutCadastrarLocal.setVisibility(View.VISIBLE);
    }
    private void showLuminariasCadastro(){
        ScaleAnimation animation = new ScaleAnimation(1.0F,1.0F,0F,1.0F,1F,1F);
        animation.setDuration(1000);
        layoutCadastrarLocal.setVisibility(View.GONE);
        layoutCadastrarLumnaria.startAnimation(animation);
        layoutCadastrarLumnaria.setVisibility(View.VISIBLE);
        setupNameDefault("");

    }
    private void showProgress(){
        progressText.setText(getString(R.string.text_aguarde));
        conectProgress.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
    }

    private void hideProgress(){
        conectProgress.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        textEmptyLumCadastro.setVisibility(View.GONE);
    }

    private void disableCheckBox(){
        lum.setEnabled(false);
        local.setEnabled(false);
    }

    private void enableCheckBox(){
        lum.setEnabled(true);
        local.setEnabled(true);
    }

    private void showProgressSalvarLocal(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                salvar.setText("");
                salvar.setEnabled(false);
                name.setEnabled(false);
                setDisableIcone();
                disableCheckBox();
                progressSalvarLocal.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showProgressSalvarLuminaria(){
        getActivity().runOnUiThread(()->{
            cadastrarLum.setText(" ");
            isCadastrandoLum = true;
            cadastrarLum.setEnabled(false);
            nameLuminaria.setEnabled(false);
            disableCheckBox();
            progressBarCadastrarLuminaria.setVisibility(View.VISIBLE);
        });
    }

    private void hideProgressSalvarLuminaria(){
        getActivity().runOnUiThread(()->{
            cadastrarLum.setText(R.string.text_cadastrar_maiusculo);
            nameLuminaria.setEnabled(true);
            scanList.setEnabled(true);
            enableCheckBox();
            isCadastrandoLum = false;
            progressBarCadastrarLuminaria.setVisibility(View.GONE);
        });
    }

    private void hideProgressSalvarLocal(){
        progressSalvarLocal.setVisibility(View.GONE);
        salvar.setText(R.string.btn_salvar);
        name.setEnabled(true);
        setEnableIcone();
        enableCheckBox();
        salvar.setEnabled(true);
    }
    private void setMensagemConect(int code){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                conectProgress.setVisibility(View.GONE);
                switch(code){
                    case Const.SUCESSO_CONECT:
                        progressText.setText(getString(R.string.text_OK));
                        break;
                    case Const.SUCESSO_DISCONECT:
                        progressText.setText(getString(R.string.text_erro));
                        break;
                    case Const.SUCESSO_ERRO:
                        progressText.setText(getString(R.string.text_erro));
                        break;
                }
            }
        });
    }

    /**
     *
     * CALLBACKS
     *
     * */
    private BleScanCallback bleScanCallback = new BleScanCallback() {
        @Override
        public void onScanFinished(List<BleDevice> scanResultList) {
            List<BleDevice> scanBleLedTec = new ArrayList<>();
            for(BleDevice result: scanResultList){
                if( result.getName()!= null && result.getName().startsWith("*")){
                    scanBleLedTec.add(result);
                }
            }
            if(devicesBd!=null) {
                for (BleDevice device : scanBleLedTec) {
                    if (devicesBd.contains(device.getMac())){
                        scanBleLedTec.remove(device);
                    }
                }
            }
            mSwipeRefreshLayout.setRefreshing(false);
            bleDeviceList.clear();
            bleDeviceList.addAll(scanBleLedTec);
            if(bleDeviceList.size()>0){
                showProgress();
                BleManager.getInstance().connect(bleDeviceList.get(0),callbackConect);
            }
            adapter.notifyDataSetChanged();
            if(adapter.getItemCount() == 0){
                textEmptyLumCadastro.setVisibility(View.VISIBLE);
            }else{
                textEmptyLumCadastro.setVisibility(View.GONE);
            }
        }

        @Override
        public void onScanStarted(boolean success) {
            if(success){
                mSwipeRefreshLayout.setRefreshing(true);
            }
        }

        @Override
        public void onScanning(BleDevice bleDevice) {

        }
    };

    @Override
    public void onBleDeviceClick(int position) {
        if(!isCadastrandoLum ) {
            showProgress();
            selecionadoBle = bleDeviceList.get(position);
            BleManager.getInstance().connect(selecionadoBle, callbackConect);
        }
    }

    private BleGattCallback callbackConect = new BleGattCallback() {
        @Override
        public void onStartConnect() {
            mSwipeRefreshLayout.setEnabled(false);
            isCadastrandoLum = true;

        }
        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {
            setMensagemConect(Const.SUCESSO_ERRO);
            selecionadoBle = null;
            adapter.updateSelect(" ");
            mSwipeRefreshLayout.setEnabled(true);
            adapter.notifyDataSetChanged();
            checkEnableButtonCadastrarLuminaria();
            isCadastrandoLum = false;
            setupNameDefault("");
        }

        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
            setMensagemConect(Const.SUCESSO_CONECT);
            selecionadoBle = bleDevice;
            adapter.updateSelect(selecionadoBle.getMac());
            mSwipeRefreshLayout.setEnabled(false);
            adapter.notifyDataSetChanged();
            checkEnableButtonCadastrarLuminaria();
            isCadastrandoLum = false;
            setupNameDefault(bleDevice.getMac());
        }

        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
            setMensagemConect(Const.SUCESSO_DISCONECT);
            selecionadoBle = null;
            adapter.updateSelect(" ");
            mSwipeRefreshLayout.setEnabled(true);
            adapter.notifyDataSetChanged();
            checkEnableButtonCadastrarLuminaria();
            isCadastrandoLum = false;
            setupNameDefault("");
        }
    };

    private BleWriteCallback writeCallback = new BleWriteCallback() {
        @Override
        public void onWriteSuccess(int current, int total, byte[] justWrite) {
            saveLuminariaDB();
        }

        @Override
        public void onWriteFailure(BleException exception) {
            hideProgressSalvarLuminaria();
            Toast.makeText(getActivity(),"Erro ao enviar dados",Toast.LENGTH_SHORT).show();

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int i =0;
    }
}
