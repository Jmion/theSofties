package ch.epfl.sweng.favors.favors;

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import ch.epfl.sweng.favors.R;
import ch.epfl.sweng.favors.database.Favor;
import ch.epfl.sweng.favors.database.FavorRequest;
import ch.epfl.sweng.favors.database.ObservableArrayList;
import ch.epfl.sweng.favors.databinding.FavorsListBinding;

/**
 * Fragment that displays the list of favor and allows User to sort it and to search in it
 */
public class FavorsList extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "FAVORS_FRAGMENT";

    FavorsListBinding binding;
    ObservableArrayList<Favor> favorList;
    FavorListAdapter listAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.favors_list,container,false);
        binding.setElements(this);

        //Spinner for sorting criteria
        Spinner sortBySpinner = binding.sortBySpinner;

        //create Spinner Adapter from resource list
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.sortBy, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortBySpinner.setAdapter(adapter);
        sortBySpinner.setOnItemSelectedListener(this);

        //button redirects to creating favor page
        binding.addNewFavor.setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FavorCreateFragment()).commit());

        binding.favorsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }

    /**
     *
     * Sorts the favors list according to sort criteria
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                FavorRequest.all(favorList, null, null);
            case 1: //location
                FavorRequest.all(favorList, null, Favor.StringFields.locationCity);
                break;
            case 2: //recent
                FavorRequest.all(favorList, null, null);
                break;
            case 3: //category
                FavorRequest.all(favorList, null, Favor.StringFields.category);
                break;
            default: break;
        }
        favorList.addOnPropertyChangedCallback(listCallBack);
    }

    /**
     *
     * Takes the favors list from the data base
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private void updateList(ObservableList<Favor> list){
        listAdapter = new FavorListAdapter(this.getActivity(), (ObservableArrayList)list);
        binding.favorsList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    Observable.OnPropertyChangedCallback listCallBack = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            updateList((ObservableList<Favor>) sender);
        }
    };
}