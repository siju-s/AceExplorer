package com.siju.acexplorer.main.view;

import com.siju.acexplorer.billing.repository.BillingManager;
import com.siju.acexplorer.storage.view.StoragesUiView;

public interface ActivityFragmentCommunicator {

    BillingManager getBillingManager();

    DrawerListener getDrawerListener();

    StoragesUiView.FavoriteOperation getFavListener();
}
