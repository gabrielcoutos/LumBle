package br.com.gabrielcouto.ledtec.homepage.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.j256.ormlite.stmt.QueryBuilder;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.gabrielcouto.ledtec.R;
import br.com.gabrielcouto.ledtec.adapter.LuminariasAdapter;
import br.com.gabrielcouto.ledtec.adapter.SwipeToDeleteCallback;
import br.com.gabrielcouto.ledtec.controle.ControleLuminaria;
import br.com.gabrielcouto.ledtec.homepage.HomePage;
import br.com.gabrielcouto.ledtec.model.dao.DatabaseHelper;
import br.com.gabrielcouto.ledtec.model.dao.LuminariaLugarDaoImpl;
import br.com.gabrielcouto.ledtec.model.entity.LuminariaLugar;


public class LuminariasFragment extends Fragment implements LuminariasAdapter.OnDeviceListener, SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerView;
    private ConstraintLayout emptyList;
    private Button btnCadastro;
    private LuminariaLugar father;
    private LuminariasAdapter luminariasAdapter;
    private List<LuminariaLugar> listPlaceAndLum = new ArrayList<>();
    private List<BleDevice> deviceList = new ArrayList<>();
    private DatabaseHelper db;
    private Comunicador comunicador;
    private ProgressBar progressBar;
    private SwipeRefreshLayout refreshLayout;
    View rootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        comunicador = (Comunicador) context;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle !=null){
            father = (LuminariaLugar) bundle.get("father");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_luminarias,null);
        db = DatabaseHelper.getInstance(getContext());
        recyclerView = rootView.findViewById(R.id.recycleViewLuminarias);
        emptyList = rootView.findViewById(R.id.layoutEmptyList);
        btnCadastro = rootView.findViewById(R.id.btnCadastrarLumLocal);
        progressBar = rootView.findViewById(R.id.idProgressBarLoadBd);
        refreshLayout = rootView.findViewById(R.id.refreshLuminarias);
        refreshLayout.setOnRefreshListener(this);
        btnCadastro.setOnClickListener((HomePage)getActivity()); // usa a função da activity mãe
        showProgress();
        setupRecycler();
        setupBle();
        loadListLumPlace();
        return rootView;
    }


    private void setupRecycler(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        luminariasAdapter = new LuminariasAdapter(listPlaceAndLum,this,deviceList,getActivity());
        recyclerView.setAdapter(luminariasAdapter);
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_recycle_anim);
        recyclerView.setLayoutAnimation(animationController);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(luminariasAdapter,getActivity()));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        setupDivider();
    }

    private void setupDivider(){
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(),R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    public void loadListLumPlace(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LuminariaLugarDaoImpl dao = new LuminariaLugarDaoImpl(db.getConnectionSource());
                    if(dao!=null){
                        QueryBuilder<LuminariaLugar,Integer> qb = dao.queryBuilder();
                        if(HomePage.nivel ==1){
                            qb.where().eq("level",HomePage.nivel);
                        }else{
                            if(father == null){
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            qb.where().eq("level",HomePage.nivel).and().eq("lumPlace_id",father.getId());
                        }
                        List<LuminariaLugar> aux1 = dao.queryForAll();
                        List<LuminariaLugar> aux = qb.query();
                        if(aux!=null && !aux.isEmpty()){
                            listPlaceAndLum.clear();
                            listPlaceAndLum.addAll(aux);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideEmptyList();
                                    luminariasAdapter.notifyDataSetChanged();
                                }
                            });
                        }else{
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showEmptyList();
                                }
                            });
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void showEmptyList(){
        emptyList.setVisibility(View.VISIBLE);
        refreshLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }
    private void hideEmptyList(){
        emptyList.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
    private void showProgress(){
        progressBar.setVisibility(View.VISIBLE);
        emptyList.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        switch(BleManager.getInstance().getScanSate()){
            case STATE_IDLE:
                BleManager.getInstance().scan(scanCallback);
                break;
            case STATE_SCANNING:
                Toast.makeText(getActivity(),"verificando... ",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            loadListLumPlace();
        }
    }


    @Override
    public void onDeviceClick(int position) {
        LuminariaLugar device = listPlaceAndLum.get(position);

        BleDevice conected = null;
        if(device.getMac() == null){
            comunicador.event(device);
        }else{
            Intent i = new Intent(getActivity(), ControleLuminaria.class);
            for (BleDevice bleDevice : deviceList){
                if(bleDevice.getMac().equals(device.getMac())){
                    conected = bleDevice;
                    i.putExtra("conectDevice",bleDevice);
                }
            }
            if(conected != null || deviceList.size() >= 1) {
                i.putExtra("conectLum", device);
                startActivity(i);
            }else{
                Toast.makeText(getActivity(),"Garanta que exista ao menos uma luminária por perto",Toast.LENGTH_SHORT).show();
            }
        }

    }
    private void setupBle(){
        BleManager.getInstance().scan(scanCallback);
    }

    private BleScanCallback scanCallback = new BleScanCallback() {
        @Override
        public void onScanFinished(List<BleDevice> scanResultList) {
            refreshLayout.setRefreshing(false);
            deviceList.clear();
            deviceList.addAll(scanResultList);
            luminariasAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanStarted(boolean success) {

        }

        @Override
        public void onScanning(BleDevice bleDevice) {

        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.excluir:
                LuminariaLugar lum = listPlaceAndLum.get(item.getGroupId());
                new AlertDialog.Builder(getActivity())
                        .setTitle("Excluir")
                        .setMessage("Tem certeza que deseja excluir "+ lum.getNome().toUpperCase()+"?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                luminariasAdapter.delete(item.getGroupId());
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
                break;
        }
        return super.onContextItemSelected(item);
    }

}
