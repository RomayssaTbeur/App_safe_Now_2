package com.example.safe_now_2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    public static class ContactItem {
        public int id;
        public String nom;
        public String telephone;

        public ContactItem(int id, String nom, String telephone) {
            this.id = id;
            this.nom = nom;
            this.telephone = telephone;
        }
    }

    private Context context;
    private List<ContactItem> contacts;
    private OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(int id, String nom);
    }

    public ContactAdapter(Context context, List<ContactItem> contacts,
                          OnDeleteListener deleteListener) {
        this.context = context;
        this.contacts = contacts;
        this.deleteListener = deleteListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ContactItem contact = contacts.get(position);

        holder.tvNom.setText(contact.nom);
        holder.tvRole.setText(contact.telephone);

        // Bouton appel
        holder.btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:" + contact.telephone));
            context.startActivity(intent);
        });

        // ✅ MODIFIÉ : Bouton chat → SOS avec message pré-rempli
        holder.btnChat.setOnClickListener(v -> {
            if (context instanceof Contact_activity) {
                // ✅ APPELE LA MÉTHODE SOS de l'Activity
                ((Contact_activity) context).envoyerSOSAUnContact(contact.telephone, contact.nom);
            } else {
                // Fallback
                Intent intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("smsto:" + contact.telephone));
                context.startActivity(intent);
            }
        });

        // Long press → supprimer
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(contact.id, contact.nom);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom, tvRole;
        CardView btnCall, btnChat;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNom = itemView.findViewById(R.id.tv_contact_name);
            tvRole = itemView.findViewById(R.id.tv_contact_role);
            btnCall = itemView.findViewById(R.id.btn_call);
            btnChat = itemView.findViewById(R.id.btn_chat);
        }
    }
}