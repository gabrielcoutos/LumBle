package br.com.gabrielcouto.ledtec.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.clj.fastble.data.BleDevice;

import java.util.List;

import br.com.gabrielcouto.ledtec.R;

public class ScanBleAdapter extends RecyclerView.Adapter<ScanBleAdapter.ScanHolder>{

    private final List<BleDevice> list;
    private OnBleDeviceListener onBleDeviceListener;
    private String select = "";

    public ScanBleAdapter(List<BleDevice> list, OnBleDeviceListener onBleDeviceListener){
        this.list = list;
        this.onBleDeviceListener = onBleDeviceListener;

    }


    @NonNull
    @Override
    public ScanHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ScanHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_scan,viewGroup,false),onBleDeviceListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanHolder scanHolder, int i) {
        if(list.get(i).getName()== null){
            scanHolder.name.setText("DESCONHECIDO");
        }else{
            scanHolder.name.setText(list.get(i).getName());
        }
        scanHolder.mac.setText(list.get(i).getMac());
        if(select.equals(list.get(i).getMac())){
            scanHolder.itemView.setBackgroundResource(R.drawable.item_scan_with_border);
        }else{
            scanHolder.itemView.setBackgroundResource(R.drawable.frame_with_radius);

        }
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public class ScanHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name,mac;
        public OnBleDeviceListener onBleDeviceListener;

        public ScanHolder(@NonNull View itemView, OnBleDeviceListener onBleDeviceListener) {
            super(itemView);
            name = itemView.findViewById(R.id.txtNameScan);
            mac = itemView.findViewById(R.id.txtMacScan);
            this.onBleDeviceListener = onBleDeviceListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            this.onBleDeviceListener.onBleDeviceClick(getAdapterPosition());
        }
    }

    public interface OnBleDeviceListener{
        void onBleDeviceClick(int position);
    }
    public void updateSelect(String macSelect){
        select = macSelect;
    }
}
