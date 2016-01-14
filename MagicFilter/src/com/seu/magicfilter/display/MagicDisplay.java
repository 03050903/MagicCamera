package com.seu.magicfilter.display;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;

import com.seu.magicfilter.filter.base.MagicFrameBuffer;
import com.seu.magicfilter.filter.factory.MagicFilterFactory;
import com.seu.magicfilter.filter.helper.MagicFilterType;
import com.seu.magicfilter.filter.helper.SaveTask;
import com.seu.magicfilter.utils.OpenGLUtils;
import com.seu.magicfilter.utils.TextureRotationUtil;

public abstract class MagicDisplay implements Renderer{
	/**
	 * ��ѡ����˾�������ΪMagicBaseGroupFilter
	 * 1.mCameraInputFilter��SurfaceTexture��YUV���ݻ��Ƶ�FrameBuffer
	 * 2.mFilters��FrameBuffer�е�������Ƶ���Ļ��
	 */
	protected final MagicFilterFactory mFilters;
	
	/**
	 * FrameBuffer����Ϊ���Ԥ��������˾������м��
	 */
	protected final MagicFrameBuffer mFrameBuffer;
	
	/**
	 * ����Ԥ�����ݻ��ƻ���
	 */
	protected final GLSurfaceView mGLSurfaceView;
	
	/**
	 * SurfaceTexure����id
	 */
	protected int mTextureId = OpenGLUtils.NO_TEXTURE;
	
	/**
	 * ��������
	 */
	protected final FloatBuffer mGLCubeBuffer;
	
	/**
	 * ��������
	 */
	protected final FloatBuffer mGLTextureBuffer;
	
	/**
	 * ���ձ���
	 */
	protected SaveTask mSaveTask;
	
	/**
	 * GLSurfaceView�Ŀ��
	 */
	protected int mSurfaceWidth, mSurfaceHeight;
	
	/**
	 * ͼ����
	 */
	protected int mImageWidth, mImageHeight;
	
	protected Context mContext;
	
	public MagicDisplay(Context context, GLSurfaceView glSurfaceView){
		mContext = context;
		mGLSurfaceView = glSurfaceView;  
		
		mFrameBuffer = MagicFrameBuffer.getInstance();
		
		mFilters = new MagicFilterFactory(context);
		
		mGLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(TextureRotationUtil.TEXTURE_NO_ROTATION).position(0);

		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setRenderer(this);
		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	/**
	 * �����˾�
	 * @param ��������
	 */
	public void setFilter(final int filterType) {
		mGLSurfaceView.queueEvent(new Runnable() {
       		
            @Override
            public void run() {
            	if(mFilters != null)
            		mFilters.destroy();
            	mFilters.setFilters(filterType);;
            	mFilters.onInit();
            	onSizeChanged();
            }
        });
		mGLSurfaceView.requestRender();
    }
	
	protected void onSizeChanged(){
		mFilters.onOutputSizeChanged(mImageWidth, mImageHeight);
    	mFrameBuffer.onInit(mImageWidth, mImageHeight, mFilters.getFilterCount());
	}
	
	protected void onResume(){
		
	}
	
	protected void onPause(){
		if(mSaveTask != null)
			mSaveTask.cancel(true);
	}
	
	protected void onDestroy(){
		
	}
	
	protected boolean isFilterSet(){
		return mFilters.getFilterType() != MagicFilterType.NONE;
	}
	
	protected void getBitmapFromGL(final Bitmap bitmap,final boolean newTexture){
		mGLSurfaceView.queueEvent(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				
				GLES20.glViewport(0, 0, width, height);			
				mFilters.onOutputSizeChanged(width, height);
            	mFrameBuffer.onInit(width, height, mFilters.getFilterCount());
            	int textureId = OpenGLUtils.NO_TEXTURE;
            	if(newTexture)
	            	textureId = OpenGLUtils.loadTexture(bitmap, OpenGLUtils.NO_TEXTURE, true);
            	else
            		textureId = mTextureId;
            	GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, MagicFrameBuffer.getFrameBuffers()[0]);
            	mFilters.onDrawFrame(textureId);
            	IntBuffer ib = IntBuffer.allocate(width * height);
                GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
                Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mBitmap.copyPixelsFromBuffer(IntBuffer.wrap(ib.array()));
                if(newTexture)
                	GLES20.glDeleteTextures(1, new int[]{textureId}, 0); 
            	GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0); 
                GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
                mFilters.destroy();
            	mFilters.onInit();
            	mFilters.onOutputSizeChanged(mImageWidth, mImageHeight);
            	mFrameBuffer.onInit(mImageWidth, mImageHeight, mFilters.getFilterCount());
            	onGetBitmapFromGL(mBitmap);
			}
		});
	}
	
	protected void onGetBitmapFromGL(Bitmap bitmap){
		
	}
	
	protected void deleteTextures() {
		if(mTextureId != OpenGLUtils.NO_TEXTURE)
			mGLSurfaceView.queueEvent(new Runnable() {
				
				@Override
				public void run() {
	                GLES20.glDeleteTextures(1, new int[]{
	                        mTextureId
	                }, 0);
	                mTextureId = OpenGLUtils.NO_TEXTURE;
	            }
	        });
    }
}
