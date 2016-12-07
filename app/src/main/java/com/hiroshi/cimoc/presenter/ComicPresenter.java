package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.ComicView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class ComicPresenter extends BasePresenter<ComicView> {

    private TagManager mTagManager;

    public ComicPresenter() {
        mTagManager = TagManager.getInstance();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_TAG_DELETE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onTagDelete((Tag) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_TAG_INSERT, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onTagInsert((Tag) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_THEME_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onThemeChange((int) rxEvent.getData(1), (int) rxEvent.getData(2));
            }
        });
    }

    public void load() {
        mCompositeSubscription.add(mTagManager.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Tag>>() {
                    @Override
                    public void call(List<Tag> list) {
                        mBaseView.onTagLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTagLoadFail();
                    }
                }));
    }

    public void filter(int type, long id) {
        if (type != TagManager.TAG_NORMAL) {
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_FILTER, type));
        } else {
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_FILTER, TagManager.TAG_NORMAL, id));
        }
    }

}
