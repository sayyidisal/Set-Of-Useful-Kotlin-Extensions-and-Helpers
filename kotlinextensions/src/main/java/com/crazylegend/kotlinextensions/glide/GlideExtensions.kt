package com.crazylegend.kotlinextensions.glide

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestOptions


/**
 * Created by x on 2/5/19 to long live and prosper !
 */

@Throws(GlideException::class)
fun Context.loadImg(imgUrl: Any, view: AppCompatImageView) {
    GlideApp.with(this)
        .load(imgUrl)
        .into(view)
}


@Throws(GlideException::class)
fun Context.loadImg(imgUrl: Any, view: AppCompatImageView, error:Drawable) {
   GlideApp.with(this)
       .load(imgUrl)
       .error(error)
       .into(view)
}

@Throws(GlideException::class)
fun Context.loadImg(imgUrl: Any, view: AppCompatImageView, error:Drawable,placeHolder:Drawable) {
   GlideApp.with(this)
       .load(imgUrl)
       .placeholder(placeHolder)
       .error(error)
       .into(view)
}


@Throws(GlideException::class)
fun Context.loadImgNoCache(imgUrl: Any, view: AppCompatImageView) {
    GlideApp.with(this)
        .applyDefaultRequestOptions(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
        .load(imgUrl)
        .into(view)
}

@Throws(GlideException::class)
fun Context.loadImgNoCache(imgUrl: Any, view: AppCompatImageView,  error:Drawable) {
    GlideApp.with(this)
        .applyDefaultRequestOptions(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
        .load(imgUrl)
        .error(error)
        .into(view)
}

@Throws(GlideException::class)
fun Context.loadImgWithTransformation(imgUrl: Any, view: AppCompatImageView, transformation: RequestOptions) {
    GlideApp.with(this)
        .applyDefaultRequestOptions(transformation)
        .load(imgUrl)
        .into(view)
}


@Throws(GlideException::class)
fun View.loadImg(imgUrl: Any, view: AppCompatImageView) {
    GlideApp.with(this)
        .load(imgUrl)
        .into(view)
}


@Throws(GlideException::class)
fun View.loadImg(imgUrl: Any, view: AppCompatImageView, error:Drawable) {
    GlideApp.with(this)
        .load(imgUrl)
        .error(error)
        .into(view)
}

@Throws(GlideException::class)
fun View.loadImg(imgUrl: Any, view: AppCompatImageView, error:Drawable,placeHolder:Drawable) {
    GlideApp.with(this)
        .load(imgUrl)
        .placeholder(placeHolder)
        .error(error)
        .into(view)
}


@Throws(GlideException::class)
fun View.loadImgNoCache(imgUrl: Any, view: AppCompatImageView) {
    GlideApp.with(this)
        .applyDefaultRequestOptions(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
        .load(imgUrl)
        .into(view)
}

@Throws(GlideException::class)
fun View.loadImgNoCache(imgUrl: Any, view: AppCompatImageView,  error:Drawable) {
    GlideApp.with(this)
        .applyDefaultRequestOptions(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
        .load(imgUrl)
        .error(error)
        .into(view)
}

@Throws(GlideException::class)
fun AppCompatImageView.loadImgNoCache(image:Any?){
    GlideApp.with(this)
            .load(image)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .into(this)
}


@Throws(GlideException::class)
fun View.loadImgWithTransformation(imgUrl: Any, view: AppCompatImageView, transformation: RequestOptions) {
    GlideApp.with(this)
        .applyDefaultRequestOptions(transformation)
        .load(imgUrl)
        .into(view)
}

