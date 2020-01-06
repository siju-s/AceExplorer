package com.siju.acexplorer.imageviewer.presenter

import com.siju.acexplorer.imageviewer.model.ImageViewerModel
import com.siju.acexplorer.imageviewer.view.ImageViewerView
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class ImageViewerPresenterImplTest {

    @Mock
    private lateinit var view : ImageViewerView
    @Mock
    private lateinit var model : ImageViewerModel

    private lateinit var presenter: ImageViewerPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = ImageViewerPresenterImpl(view, model)
    }

    @Test
    fun testInflateView() {
        presenter.inflateView()
        verify(view).inflate()
    }

    @Test
    fun testLoadInfo() {
        presenter.loadInfo("test")
        verify(model).loadInfo("test")
    }

    @Test
    fun testDeleteFile() {
        presenter.deleteFile("test")
        verify(model).deleteFile("test")
    }

    @Test
    fun testShareClicked() {
        presenter.shareClicked("test")
        verify(model).shareClicked("test")
    }

    @Test
    fun testShareClickedWithNullUri() {
        presenter.shareClicked(null)
        verify(model, never()).shareClicked("test")
    }
}