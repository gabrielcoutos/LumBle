package br.com.gabrielcouto.ledtec.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.data.BleDevice;

import java.sql.SQLException;
import java.util.List;

import br.com.gabrielcouto.ledtec.R;
import br.com.gabrielcouto.ledtec.model.dao.DatabaseHelper;
import br.com.gabrielcouto.ledtec.model.dao.LuminariaLugarDaoImpl;
import br.com.gabrielcouto.ledtec.model.entity.LuminariaLugar;
import br.com.gabrielcouto.ledtec.utils.Const;
import br.com.gabrielcouto.ledtec.utils.Utils;

public class LuminariasAdapter extends RecyclerView.Adapter<LuminariasAdapter.LuminariasHolder>  {

    private List<LuminariaLugar> lumLugarList ;
    private OnDeviceListener mOnDeviceListener;
    private List<BleDevice> deviceOn;
    private Context context;

    public LuminariasAdapter(@NonNull List<LuminariaLugar> list, OnDeviceListener onDeviceListener, @NonNull List<BleDevice> deviceOn,Context context){
        this.lumLugarList = list;
        this.mOnDeviceListener =onDeviceListener;
        this.deviceOn =deviceOn;
        this.context =context;
    }

    @NonNull
    @Override
    public LuminariasHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new LuminariasHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_luminarias_lugares,viewGroup,false),mOnDeviceListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LuminariasHolder luminariasHolder, int i) {
        luminariasHolder.device.setText(lumLugarList.get(i).getNome());
        switch (lumLugarList.get(i).getIcone()){
            case Const.ICONE_CASA:
                luminariasHolder.icon.setImageResource(R.drawable.house_black);
                break;
            case Const.ICONE_BANHEIRO:
                luminariasHolder.icon.setImageResource(R.drawable.banheiro_black);
                break;
            case Const.ICONE_LUM:
                if(lumLugarList.get(i).getMac()!= null){
                    for(BleDevice aux : deviceOn){
                        if(aux.getMac().equals(lumLugarList.get(i).getMac())) {
                            luminariasHolder.icon.setImageResource(R.drawable.lum_device_blue);
                            break;
                        }
                        else
                            luminariasHolder.icon.setImageResource(R.drawable.lum_device);
                    }
                    if(deviceOn.size() == 0){
                        luminariasHolder.icon.setImageResource(R.drawable.lum_device);
                    }
                }else
                    luminariasHolder.icon.setImageResource(R.drawable.lum_device);
                break;
            case Const.ICONE_ESCRITORIO:
                luminariasHolder.icon.setImageResource(R.drawable.escritorio_black);
                break;
            case Const.ICONE_GARAGEM:
                luminariasHolder.icon.setImageResource(R.drawable.garagem_black);
                break;
            case Const.ICONE_QUARTO:
                luminariasHolder.icon.setImageResource(R.drawable.quarto_black);
                break;
            case Const.ICONE_TV:
                luminariasHolder.icon.setImageResource(R.drawable.tv_black);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return lumLugarList.size();
    }

    public class LuminariasHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        public TextView device;
        public ImageView icon;
        public OnDeviceListener onDeviceListener;

        public LuminariasHolder(View itemView, OnDeviceListener onDeviceListener) {
            super(itemView);
            device = (TextView) itemView.findViewById(R.id.textViewDevice);
            icon = itemView.findViewById(R.id.imgIcon);
            this.onDeviceListener =onDeviceListener;
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            onDeviceListener.onDeviceClick(getAdapterPosition());
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(),R.id.excluir, Menu.NONE,R.string.txt_excluir);
        }
    }

    public interface OnDeviceListener{
        void onDeviceClick(int position);
    }

    public void delete(int pos){
        LuminariaLugar removeItem = lumLugarList.get(pos);
        DatabaseHelper dh = DatabaseHelper.getInstance(context);
        try {
            LuminariaLugarDaoImpl dao = new LuminariaLugarDaoImpl(dh.getConnectionSource());
            int valor = dao.deleteById(removeItem.getId());
            if(valor!= -1){
                lumLugarList.remove(pos);
                notifyItemRemoved(pos);
            }else{
                Toast.makeText(context,"Erro ao excluir",Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
            notifyDataSetChanged();
        }
    }







}
