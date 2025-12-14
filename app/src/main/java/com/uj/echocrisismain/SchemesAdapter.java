package com.uj.echocrisismain;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SchemesAdapter extends RecyclerView.Adapter<SchemesAdapter.ViewHolder> {

    public interface OnSchemeClickListener {
        void onSchemeClick(SchemeModel scheme);
    }

    private List<SchemeModel> schemes;
    private OnSchemeClickListener listener;

    public SchemesAdapter(List<SchemeModel> schemes, OnSchemeClickListener listener) {
        this.schemes = schemes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scheme, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SchemeModel scheme = schemes.get(position);

        holder.title.setText(scheme.getTitle());
        holder.description.setText(scheme.getDescription());
        holder.eligibility.setText("Eligibility: " + scheme.getEligibility());

        // ✅ Set link text
        holder.link.setText(scheme.getLink());

        // ✅ Open website when clicking the link
        holder.link.setOnClickListener(v -> {
            String url = scheme.getLink();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url; // Ensure valid format
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            holder.itemView.getContext().startActivity(intent);
        });

        // If you still want whole card click to work
        holder.itemView.setOnClickListener(v -> listener.onSchemeClick(scheme));
    }

    @Override
    public int getItemCount() {
        return schemes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, eligibility, link;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.schemeTitle);
            description = itemView.findViewById(R.id.schemeDescription);
            eligibility = itemView.findViewById(R.id.schemeEligibility);
            link = itemView.findViewById(R.id.schemeLink); // ✅ New link field
        }
    }
}
