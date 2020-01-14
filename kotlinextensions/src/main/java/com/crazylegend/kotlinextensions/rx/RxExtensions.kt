package com.crazylegend.kotlinextensions.rx

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import com.crazylegend.kotlinextensions.database.*
import com.crazylegend.kotlinextensions.retrofit.*
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.Subject
import org.reactivestreams.Publisher
import retrofit2.Response
import java.util.concurrent.TimeUnit


/**
 * Created by hristijan on 3/5/19 to long live and prosper !
 */


fun <T : Any> Observable<T>.joinToString(
        separator: String? = null,
        prefix: String? = null,
        postfix: String? = null
): Single<String> = collect(
        { StringBuilder(prefix ?: "") })
{ builder: StringBuilder, next: T ->
    builder.append(if (builder.length == prefix?.length ?: 0) "" else separator ?: "").append(next)
}
        .map { it.append(postfix ?: "").toString() }


fun <T : Any> Observable<T>.withIndex(): Observable<IndexedValue<T>> =
        zipWith(Observable.range(0, Int.MAX_VALUE), BiFunction { value, index -> IndexedValue(index, value) })


val mainThreadScheduler = AndroidSchedulers.mainThread()
val newThreadScheduler = Schedulers.newThread()
val ioThreadScheduler = Schedulers.io()
val computationScheduler = Schedulers.computation()
val trampolineScheduler = Schedulers.trampoline()


/**
 * observe on main thread
 * subscribe on new thread
 * unsubsidised on error and on complete and removes the need to handle it afterwards
 * @usage
 * someObservable
 *  .runSafeOnMain()
 *  .subscribe({}, {])
 */
fun <T> Observable<T>.runSafeOnMain(subscribeOn: Scheduler = newThreadScheduler): Observable<T> =
        observeOn(mainThreadScheduler)
                .subscribeOn(subscribeOn)
                .doOnError { unsubscribeOn(newThreadScheduler) }
                .doOnComplete { unsubscribeOn(newThreadScheduler) }

fun <T> Observable<T>.runSafeOnIO(subscribeOn: Scheduler = newThreadScheduler): Observable<T> =
        observeOn(ioThreadScheduler)
                .subscribeOn(subscribeOn)
                .doOnError { unsubscribeOn(newThreadScheduler) }
                .doOnComplete { unsubscribeOn(newThreadScheduler) }

fun <T> Flowable<T>.runSafeOnMain(subscribeOn: Scheduler = newThreadScheduler): Flowable<T> =
        observeOn(mainThreadScheduler)
                .subscribeOn(subscribeOn)
                .doOnError { unsubscribeOn(newThreadScheduler) }
                .doOnComplete { unsubscribeOn(newThreadScheduler) }

fun <T> Flowable<T>.runSafeOnIO(subscribeOn: Scheduler = newThreadScheduler): Flowable<T> =
        observeOn(ioThreadScheduler)
                .subscribeOn(subscribeOn)
                .doOnError { unsubscribeOn(newThreadScheduler) }
                .doOnComplete { unsubscribeOn(newThreadScheduler) }

fun <T> Single<T>.runSafeOnMain(subscribeOn: Scheduler = newThreadScheduler): Single<T> =
        observeOn(mainThreadScheduler)
                .subscribeOn(subscribeOn)
                .doOnError { unsubscribeOn(newThreadScheduler) }
                .doOnSuccess { unsubscribeOn(newThreadScheduler) }

fun <T> Single<T>.runSafeOnIO(subscribeOn: Scheduler = newThreadScheduler): Single<T> =
        observeOn(ioThreadScheduler)
                .subscribeOn(subscribeOn)
                .doOnError { unsubscribeOn(newThreadScheduler) }
                .doOnSuccess { unsubscribeOn(newThreadScheduler) }

fun Completable.runSafeOnMain(subscribeOn: Scheduler = newThreadScheduler): Completable =
        observeOn(mainThreadScheduler)
                .subscribeOn(subscribeOn)
                .doOnError { unsubscribeOn(newThreadScheduler) }
                .doOnComplete { unsubscribeOn(newThreadScheduler) }

fun Completable.runSafeOnIO(subscribeOn: Scheduler = newThreadScheduler): Completable =
        observeOn(ioThreadScheduler)
                .subscribeOn(subscribeOn)
                .doOnError { unsubscribeOn(newThreadScheduler) }
                .doOnComplete { unsubscribeOn(newThreadScheduler) }

fun <T> Maybe<T>.runSafeOnMain(subscribeOn: Scheduler = newThreadScheduler): Maybe<T> =
        observeOn(mainThreadScheduler)
                .subscribeOn(subscribeOn)
                .doOnError { unsubscribeOn(newThreadScheduler) }
                .doOnSuccess { unsubscribeOn(newThreadScheduler) }

fun <T> Maybe<T>.runSafeOnIO(subscribeOn: Scheduler = newThreadScheduler): Maybe<T> =
        observeOn(ioThreadScheduler)
                .subscribeOn(subscribeOn)
                .doOnError { unsubscribeOn(newThreadScheduler) }
                .doOnSuccess { unsubscribeOn(newThreadScheduler) }


fun Disposable?.unsubscribe() {
    this?.let {
        if (!isDisposed) {
            dispose()
        }
    }
}

fun <T> Observable<T>.asFlowable(backpressureStrategy: BackpressureStrategy = BackpressureStrategy.LATEST)
        : Flowable<T> {
    return this.toFlowable(backpressureStrategy)
}

fun <T> Flowable<T>.asLiveData(): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this)
}

fun <T> Observable<T>.asLiveData(backpressureStrategy: BackpressureStrategy = BackpressureStrategy.LATEST)
        : LiveData<T> {

    return LiveDataReactiveStreams.fromPublisher(this.toFlowable(backpressureStrategy))

}

fun <T, R> Flowable<List<T>>.mapToList(mapper: (T) -> R): Flowable<List<R>> {
    return this.map { it.map { mapper(it) } }
}

fun <T, R> Observable<List<T>>.mapToList(mapper: (T) -> R): Observable<List<R>> {
    return this.map { it.map { mapper(it) } }
}

fun <T, R> Single<List<T>>.mapToList(mapper: ((T) -> R)): Single<List<R>> {
    return flatMap { Flowable.fromIterable(it).map(mapper).toList() }
}


fun <T> Observable<T>.defer(): Observable<T> {
    return Observable.defer { this }
}

fun <T> Single<T>.defer(): Single<T> {
    return Single.defer { this }
}

fun rxTimer(
        oldTimer: Disposable?,
        time: Long,
        unit: TimeUnit = TimeUnit.MILLISECONDS,
        thread: Scheduler = Schedulers.computation(),
        observerThread: Scheduler = mainThreadScheduler, action: ((Long) -> Unit)
): Disposable? {
    oldTimer?.dispose()
    return Observable
            .timer(time, unit, thread)
            .observeOn(observerThread)
            .subscribe {
                action.invoke(it)
            }
}

val Disposable?.isNullOrDisposed get() = this == null || isDisposed

val <T> Subject<T>.canPublish get() = !hasComplete() && !hasThrowable()


// region Single
/**
 * Calls subscribe on `this`, with an empty function for both onSuccess and onError.
 */
@Suppress("CheckResult")
fun <T> Single<T>.subscribeIgnoringResult() {
    subscribe({}, {})
}

/**
 * Chains a flapMap to `this` if [predicate] is true, applying [mapper] to the item emitted by the source Single.
 *
 * @param predicate whether or not the mapper function will be applied
 * @param mapper function to transform the emitter item
 *
 * @return the new Single if [predicate] is true, the original Single otherwise
 */
fun <T> Single<T>.flatMapIf(predicate: Boolean, mapper: (T) -> Single<T>): Single<T> =
        if (predicate) flatMap { mapper(it) } else this
// endregion

// region Completable
/**
 * Calls subscribe on `this`, with an empty function for both onComplete and onError.
 */
@Suppress("CheckResult")
fun Completable.subscribeIgnoringResult() {
    subscribe({}, {})
}

/**
 * Creates a Single that will subscribe to `this` and emit after `this` completes.
 *
 * @param item value to be emitted by onSuccess once source Completable calls onComplete
 * @param alternateError optional throwable to be emitted by onError once source Completable call onError. If null then
 * original error is emitted.
 *
 * @return the new Single
 */
fun <T> Completable.emitOnComplete(item: T, alternateError: Throwable? = null): Single<T> =
        Single.create { emitter ->
            subscribe(
                    { emitter.ifNotDisposed { onSuccess(item) } },
                    { error -> emitter.ifNotDisposed { onError(alternateError ?: error) } }
            )
        }

/**
 * Creates a Single that will subscribe to `this` and always call onError after `this` completes, regardless of
 * onComplete or onError.
 *
 * @param error throwable to be emitted by onError once source Completable calls onComplete or onError
 *
 * @return the new Completable
 */
fun <T> Completable.emitErrorOnComplete(error: Throwable): Single<T> =
        Single.create { emitter ->
            subscribe(
                    { emitter.ifNotDisposed { onError(error) } },
                    { emitter.ifNotDisposed { onError(error) } }
            )
        }

/**
 * Creates a Single that will subscribe to `this` and always call onSuccess after `this` completes, regardless of
 * onComplete or onError.
 *
 * @param item value to be emitted by onSuccess once source Completable calls onComplete or onError
 *
 * @return the new Completable
 */
fun <T> Completable.emitFinally(item: T): Single<T> =
        Single.create { emitter ->
            subscribe(
                    { emitter.ifNotDisposed { onSuccess(item) } },
                    { emitter.ifNotDisposed { onSuccess(item) } }
            )
        }

/**
 * Creates a Completable that will subscribe to `this` and once onComplete is called then subscribe will be called on
 * [chainableCompletableInvocation]. The new completable will emit the values emitted by [chainableCompletableInvocation].
 *
 * @param chainableCompletableInvocation Completable source that will only be subscribed to if `this` completes without
 * error
 *
 * @return the new Completable
 */
fun Completable.ifCompletes(chainableCompletableInvocation: () -> Completable): Completable =
        Completable.create { emitter ->
            subscribe(
                    {
                        chainableCompletableInvocation().subscribe(
                                { emitter.ifNotDisposed { onComplete() } },
                                { error -> emitter.ifNotDisposed { onError(error) } }
                        )
                    },
                    { error -> emitter.ifNotDisposed { onError(error) } }
            )
        }
// endregion

// region Observable
/**
 * Calls subscribe on `this`, with an empty function for onNext and onError
 */
@Suppress("CheckResult")
fun <T> Observable<T>.subscribeIgnoringResult() {
    subscribe({}, {})
}

/**
 * Function to peek emissions and possibly filter them out based on a given [predicate].
 *
 * @param predicate filter function to decide whether a given emission will actually be emitted
 */
inline fun <T> Observable<T>.filterNotifications(crossinline predicate: (Notification<T>) -> Boolean): Observable<T> =
        materialize().filter { predicate(it) }.dematerialize { it }
// endregion

// region Emitters
/**
 * Checks if `this` is already disposed before invoking [body].
 *
 * @param body that will only be invoked if isDisposed is false
 */
inline fun <T> MaybeEmitter<T>.ifNotDisposed(body: MaybeEmitter<T>.() -> Unit) {
    if (!isDisposed) body()
}

/**
 * Checks if `this` is already disposed before invoking [body].
 *
 * @param body that will only be invoked if isDisposed is false
 */
inline fun <T> SingleEmitter<T>.ifNotDisposed(body: SingleEmitter<T>.() -> Unit) {
    if (!isDisposed) body()
}

/**
 * Checks if `this` is already disposed before invoking [body].
 *
 * @param body that will only be invoked if isDisposed is false
 */
inline fun CompletableEmitter.ifNotDisposed(body: CompletableEmitter.() -> Unit) {
    if (!isDisposed) body()
}

/**
 * Checks if `this` is already disposed before invoking [body].
 *
 * @param body that will only be invoked if isDisposed is false
 */
inline fun <T> ObservableEmitter<T>.ifNotDisposed(body: ObservableEmitter<T>.() -> Unit) {
    if (!isDisposed) body()
}


/**
 *  In this scenario, you must also manage the Rx publisher subscription across the life of your fragment or activity.
 *  Note that LiveData does NOT handle errors and it expects that errors are treated as states in the data that's held.
 *  In case of an error being emitted by the publisher, an error will be propagated to the main thread and the app will crash.
 * @receiver Flowable<T>
 * @return LiveData<T>
 */
inline fun <reified T> Flowable<T>.asReactivePublisher(): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this)
}

/**
 * Flowables, most consumers will be able to let the library deal with backpressure using operators and not need to worry about ever manually calling request(long).
 * @receiver LiveData<T>
 *     On subscription to the publisher, the observer will attach to the given LiveData.
 *     Once {@link Subscription#request) is called on the subscription object, an observer will be connected to the data stream.
 *     Calling request(Long.MAX_VALUE) is equivalent to creating an unbounded stream with no backpressure.
 *     If request with a finite count reaches 0, the observer will buffer the latest item and emit it to the subscriber when data is again requested.
 *     Any other items emitted during the time there was no backpressure requested will be dropped.
 * @param lifecycleOwner LifecycleOwner
 * @return Publisher<T>
 */
inline fun <reified T> LiveData<T>.toReactivePublisher(lifecycleOwner: LifecycleOwner): Publisher<T> {
    return LiveDataReactiveStreams.toPublisher(lifecycleOwner, this)
}


/**
 *
 * @receiver Flowable<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param dropBackPressure Boolean
 * @param includeEmptyData Boolean
 */
fun <T : Response<R>, R> Flowable<T>?.makeApiCall(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        dropBackPressure: Boolean = false,
        includeEmptyData: Boolean = false
) {
    result.loading()

    this?.let { call ->

        if (dropBackPressure) {
            call.onBackpressureDrop()
        }
        call.observeOn(mainThreadScheduler)
                .subscribe({
                    result.subscribe(it, includeEmptyData)
                }, {
                    result.callError(it)
                }).addTo(compositeDisposable)
    }

}


fun <T : Response<R>, R> Flowable<T>?.makeApiCallList(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        dropBackPressure: Boolean = false,
        includeEmptyData: Boolean = true
) {
    result.loading()

    this?.let { call ->

        if (dropBackPressure) {
            call.onBackpressureDrop()
        }
        call.observeOn(mainThreadScheduler)
                .subscribe({
                    result.subscribeList(it, includeEmptyData)
                }, {
                    result.callError(it)
                }).addTo(compositeDisposable)
    }

}

/**
 *
 * @receiver Single<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T : Response<R>, R> Single<T>?.makeApiCall(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.loading()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribe(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)

}

fun <T : Response<R>, R> Single<T>?.makeApiCallList(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.loading()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribe(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)

}

/**
 *
 * @receiver Observable<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T : Response<R>, R> Observable<T>?.makeApiCall(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.loading()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribe(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)

}

fun <T : Response<R>, R> Observable<T>?.makeApiCallList(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.loading()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribe(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)

}

/**
 *
 * @receiver Maybe<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T : Response<R>, R> Maybe<T>?.makeApiCall(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.loading()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribe(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)

}

fun <T : Response<R>, R> Maybe<T>?.makeApiCallList(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.loading()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribe(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)

}

/**
 *
 * @receiver CompositeDisposable
 * @param result MutableLiveData<RetrofitResult<T>>
 * @param dropBackPressure Boolean
 * @param includeEmptyData Boolean
 * @param function Function0<Flowable<Response<T>>?>
 */
fun <T> CompositeDisposable.makeApiCall(result: MutableLiveData<RetrofitResult<T>>, dropBackPressure: Boolean = false,
                                        includeEmptyData: Boolean = false, function: () -> Flowable<Response<T>>?) {
    result.loading()
    val disposable = function()
            ?.observeOn(mainThreadScheduler)


    if (dropBackPressure) {
        disposable?.onBackpressureDrop()
                ?.subscribe({
                    result.subscribe(it, includeEmptyData)
                }, {
                    result.callError(it)
                })
                ?.addTo(this)
    } else {
        disposable
                ?.subscribe({
                    result.subscribe(it, includeEmptyData)
                }, {
                    result.callError(it)
                })
                ?.addTo(this)
    }

}

fun <T> CompositeDisposable.makeApiCallList(result: MutableLiveData<RetrofitResult<T>>, dropBackPressure: Boolean = false,
                                            includeEmptyData: Boolean = true, function: () -> Flowable<Response<T>>?) {
    result.loading()
    val disposable = function()
            ?.observeOn(mainThreadScheduler)


    if (dropBackPressure) {
        disposable?.onBackpressureDrop()
                ?.subscribe({
                    result.subscribeList(it, includeEmptyData)
                }, {
                    result.callError(it)
                })
                ?.addTo(this)
    } else {
        disposable
                ?.subscribe({
                    result.subscribeList(it, includeEmptyData)
                }, {
                    result.callError(it)
                })
                ?.addTo(this)
    }

}


/**
 *
 * @receiver CompositeDisposable
 * @param result MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @param function Function0<Single<Response<T>>?>
 */
fun <T> CompositeDisposable.makeApiCallSingle(result: MutableLiveData<RetrofitResult<T>>,
                                              includeEmptyData: Boolean = false, function: () -> Single<Response<T>>?) {
    result.loading()
    function()
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribe(it, includeEmptyData)
            }, {
                result.callError(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeApiCallListSingle(result: MutableLiveData<RetrofitResult<T>>,
                                                  includeEmptyData: Boolean = true, function: () -> Single<Response<T>>?) {
    result.loading()
    function()
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribeList(it, includeEmptyData)
            }, {
                result.callError(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeApiCallMaybe(result: MutableLiveData<RetrofitResult<T>>,
                                             includeEmptyData: Boolean = false, function: () -> Maybe<Response<T>>?) {
    result.loading()
    function()
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribe(it, includeEmptyData)
            }, {
                result.callError(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeApiCallListMaybe(result: MutableLiveData<RetrofitResult<T>>,
                                                 includeEmptyData: Boolean = true, function: () -> Maybe<Response<T>>?) {
    result.loading()
    function()
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribeList(it, includeEmptyData)
            }, {
                result.callError(it)
            })
            ?.addTo(this)
}


/**
 *
 * @receiver Flowable<T>?
 * @param result MutableLiveData<DBResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param dropBackPressure Boolean
 * @param includeEmptyData Boolean
 */
fun <T> Flowable<T>?.makeDBCall(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        dropBackPressure: Boolean = false,
        includeEmptyData: Boolean = false
) {
    result.querying()
    this?.let { call ->
        if (dropBackPressure) {
            call.onBackpressureDrop()
        }
        call.subscribeOn(ioThreadScheduler)
                .observeOn(mainThreadScheduler)
                .subscribe({
                    result.subscribe(it, includeEmptyData)
                }, {
                    result.callError(it)
                }).addTo(compositeDisposable)
    }
}

fun <T> Flowable<T>?.makeDBCallList(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        dropBackPressure: Boolean = false,
        includeEmptyData: Boolean = true
) {
    result.querying()
    this?.let { call ->
        if (dropBackPressure) {
            call.onBackpressureDrop()
        }
        call.subscribeOn(ioThreadScheduler)
                .observeOn(mainThreadScheduler)
                .subscribe({
                    result.subscribeList(it, includeEmptyData)
                }, {
                    result.callError(it)
                }).addTo(compositeDisposable)
    }
}

/**
 *
 * @receiver Single<T>?
 * @param result MutableLiveData<DBResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T> Single<T>?.makeDBCall(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.querying()
    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribe(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)

}

fun <T> Single<T>?.makeDBCallList(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.querying()
    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribeList(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)

}

/**
 *
 * @receiver Observable<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T> Observable<T>?.makeDBCall(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.querying()

    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribe(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)

}

fun <T> Observable<T>?.makeDBCallList(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.querying()
    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribeList(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)

}

/**
 *
 * @receiver Maybe<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T> Maybe<T>?.makeDBCall(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.querying()
    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribe(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)
}

fun <T> Maybe<T>?.makeDBCallList(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.querying()

    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribeList(it, includeEmptyData)
    }, {
        result.callError(it)
    })?.addTo(compositeDisposable)
}


fun <T> CompositeDisposable.makeDBCall(result: MutableLiveData<DBResult<T>>, dropBackPressure: Boolean = false,
                                       includeEmptyData: Boolean = false, function: () -> Flowable<T>?) {
    result.querying()
    val disposable = function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)

    if (dropBackPressure) {
        disposable?.onBackpressureDrop()
                ?.subscribe({
                    result.subscribe(it, includeEmptyData)
                }, {
                    result.callError(it)
                })
                ?.addTo(this)
    } else {
        disposable
                ?.subscribe({
                    result.subscribe(it, includeEmptyData)
                }, {
                    result.callError(it)
                })
                ?.addTo(this)
    }

}

fun <T> CompositeDisposable.makeDBCallList(result: MutableLiveData<DBResult<T>>, dropBackPressure: Boolean = false,
                                           includeEmptyData: Boolean = false, function: () -> Flowable<T>?) {
    result.querying()
    val disposable = function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)

    if (dropBackPressure) {
        disposable?.onBackpressureDrop()
                ?.subscribe({
                    result.subscribeList(it, includeEmptyData)
                }, {
                    result.callError(it)
                })
                ?.addTo(this)
    } else {
        disposable
                ?.subscribe({
                    result.subscribeList(it, includeEmptyData)
                }, {
                    result.callError(it)
                })
                ?.addTo(this)
    }

}

fun <T> CompositeDisposable.makeDBCallSingle(result: MutableLiveData<DBResult<T>>,
                                             includeEmptyData: Boolean = false, function: () -> Single<T>?) {
    result.querying()
    function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribe(it, includeEmptyData)
            }, {
                result.callError(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeDBCallListSingle(result: MutableLiveData<DBResult<T>>,
                                                 includeEmptyData: Boolean = true, function: () -> Single<T>?) {
    result.querying()
    function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribeList(it, includeEmptyData)
            }, {
                result.callError(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeDBCallMaybe(result: MutableLiveData<DBResult<T>>,
                                            includeEmptyData: Boolean = false, function: () -> Maybe<T>?) {
    result.querying()
    function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribe(it, includeEmptyData)
            }, {
                result.callError(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeDBCallListMaybe(result: MutableLiveData<DBResult<T>>,
                                                includeEmptyData: Boolean = true, function: () -> Maybe<T>?) {
    result.querying()
    function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribeList(it, includeEmptyData)
            }, {
                result.callError(it)
            })
            ?.addTo(this)
}

fun Completable?.makeDBCall(compositeDisposable: CompositeDisposable, onThrow: (error: Throwable) -> Unit = { _ -> }, onComplete: () -> Unit = {}) {
    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        onComplete()
    }, {
        onThrow.invoke(it)
    })?.addTo(compositeDisposable)
}


fun CompositeDisposable.makeDBCallCompletable(onComplete: () -> Unit = {}, onThrow: (error: Throwable) -> Unit = { _ -> }, function: () -> Completable?) {
    function()?.subscribeOn(ioThreadScheduler)
                ?.observeOn(mainThreadScheduler)
                ?.subscribe({
                    onComplete()
                }, {
                    onThrow.invoke(it)
                })?.addTo(this@makeDBCallCompletable)
}


fun <T> Observable<T>.toFlowableLatest(): Flowable<T> = toFlowable(BackpressureStrategy.LATEST)
fun <T> Observable<T>.toFlowableBuffer(): Flowable<T> = toFlowable(BackpressureStrategy.BUFFER)
fun <T> Observable<T>.toFlowableDrop(): Flowable<T> = toFlowable(BackpressureStrategy.DROP)
fun <T> Observable<T>.toFlowableError(): Flowable<T> = toFlowable(BackpressureStrategy.ERROR)
fun <T> Observable<T>.toFlowableMissing(): Flowable<T> = toFlowable(BackpressureStrategy.MISSING)


fun <T> Maybe<T>.toFlowableLatest() = toObservable().toFlowableLatest()
fun <T> Maybe<T>.toFlowableBuffer() = toObservable().toFlowableBuffer()
fun <T> Maybe<T>.toFlowableDrop() = toObservable().toFlowableDrop()
fun <T> Maybe<T>.toFlowableError() = toObservable().toFlowableError()
fun <T> Maybe<T>.toFlowableMissing() = toObservable().toFlowableMissing()

fun <T> Single<T>.toFlowableLatest() = toObservable().toFlowableLatest()
fun <T> Single<T>.toFlowableBuffer() = toObservable().toFlowableBuffer()
fun <T> Single<T>.toFlowableDrop() = toObservable().toFlowableDrop()
fun <T> Single<T>.toFlowableError() = toObservable().toFlowableError()
fun <T> Single<T>.toFlowableMissing() = toObservable().toFlowableMissing()


fun <T, R> Maybe<T>.mapSelf(mapper: T.() -> R) = map { it.mapper() }

fun <T> Subject<T>.canPublish(): Boolean = !hasComplete() && !hasThrowable()


fun <T> Observable<T>?.subscribeSafely() = this?.subscribe({}, {})

fun <T> Observable<T>?.subscribeSafely(consumer: Consumer<T>) =
        this?.subscribe(consumer, Consumer<Throwable> { })

inline fun <reified T> Observable<T>.applyNetworkSchedulers(): Observable<T> {
    return this.subscribeOn(ioThreadScheduler)
            .observeOn(mainThreadScheduler)
}

inline fun <reified T> Observable<T>.applyComputationSchedulers(): Observable<T> {
    return this.subscribeOn(Schedulers.computation()).observeOn(mainThreadScheduler)
}

inline fun <reified T> Observable<T>.intervalRequest(duration: Long): Observable<T> {
    return this.throttleFirst(duration, TimeUnit.MILLISECONDS)
}

inline fun <reified T> Flowable<T>.applyNetworkSchedulers(): Flowable<T> {
    return this.subscribeOn(ioThreadScheduler)
            .observeOn(mainThreadScheduler)
}

inline fun <reified T> Flowable<T>.applyComputationSchedulers(): Flowable<T> {
    return this.subscribeOn(Schedulers.computation()).observeOn(mainThreadScheduler)
}

inline fun <reified T> Flowable<T>.intervalRequest(duration: Long): Flowable<T> {
    return this.throttleFirst(duration, TimeUnit.MILLISECONDS)
}

inline fun <reified T> Single<T>.applyNetworkSchedulers(): Single<T> {
    return this.subscribeOn(ioThreadScheduler)
            .observeOn(mainThreadScheduler)
}

inline fun <reified T> Single<T>.applyComputationSchedulers(): Single<T> {
    return this.subscribeOn(Schedulers.computation()).observeOn(mainThreadScheduler)
}


inline fun <reified T> Maybe<T>.applyNetworkSchedulers(): Maybe<T> {
    return this.subscribeOn(ioThreadScheduler)
            .observeOn(mainThreadScheduler)
}

inline fun <reified T> Maybe<T>.applyComputationSchedulers(): Maybe<T> {
    return this.subscribeOn(Schedulers.computation()).observeOn(mainThreadScheduler)
}


fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Flowable<Response<T>>,
        secondCall: Flowable<Response<U>>,
        onError: (throwable: Throwable) -> Unit,
        onFirstCall: (model: Response<T>) -> Unit,
        onSecondCall: (model: Response<U>) -> Unit
) {
    Flowable.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                onFirstCall(it.first)
                onSecondCall(it.second)
            }, {
                onError(it)
            }).addTo(this)
}

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Flowable<Response<T>>,
        secondCall: Flowable<Response<U>>,
        firstResult: MutableLiveData<RetrofitResult<T>>,
        secondResult: MutableLiveData<RetrofitResult<U>>,
        onError: (throwable: Throwable) -> Unit
) {
    Flowable.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                firstResult.subscribe(it.first)
                secondResult.subscribe(it.second)
            }, {
                onError(it)
            }).addTo(this)
}

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Flowable<Response<T>>,
        secondCall: Flowable<Response<U>>,
        firstResult: MutableLiveData<RetrofitResult<T>>,
        secondResult: MutableLiveData<RetrofitResult<U>>
) {
    Flowable.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                firstResult.subscribe(it.first)
                secondResult.subscribe(it.second)
            }, {
                firstResult.callError(it)
                secondResult.callError(it)
            }).addTo(this)
}

//

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Observable<Response<T>>,
        secondCall: Observable<Response<U>>,
        onError: (throwable: Throwable) -> Unit,
        onFirstCall: (model: Response<T>) -> Unit,
        onSecondCall: (model: Response<U>) -> Unit
) {
    Observable.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                onFirstCall(it.first)
                onSecondCall(it.second)
            }, {
                onError(it)
            }).addTo(this)
}

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Observable<Response<T>>,
        secondCall: Observable<Response<U>>,
        firstResult: MutableLiveData<RetrofitResult<T>>,
        secondResult: MutableLiveData<RetrofitResult<U>>,
        onError: (throwable: Throwable) -> Unit
) {
    Observable.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                firstResult.subscribe(it.first)
                secondResult.subscribe(it.second)
            }, {
                onError(it)
            }).addTo(this)
}

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Observable<Response<T>>,
        secondCall: Observable<Response<U>>,
        firstResult: MutableLiveData<RetrofitResult<T>>,
        secondResult: MutableLiveData<RetrofitResult<U>>
) {
    Observable.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                firstResult.subscribe(it.first)
                secondResult.subscribe(it.second)
            }, {
                firstResult.callError(it)
                secondResult.callError(it)
            }).addTo(this)
}

//

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Single<Response<T>>,
        secondCall: Single<Response<U>>,
        onError: (throwable: Throwable) -> Unit,
        onFirstCall: (model: Response<T>) -> Unit,
        onSecondCall: (model: Response<U>) -> Unit
) {
    Single.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                onFirstCall(it.first)
                onSecondCall(it.second)
            }, {
                onError(it)
            }).addTo(this)
}

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Single<Response<T>>,
        secondCall: Single<Response<U>>,
        firstResult: MutableLiveData<RetrofitResult<T>>,
        secondResult: MutableLiveData<RetrofitResult<U>>,
        onError: (throwable: Throwable) -> Unit
) {
    Single.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                firstResult.subscribe(it.first)
                secondResult.subscribe(it.second)
            }, {
                onError(it)
            }).addTo(this)
}

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Single<Response<T>>,
        secondCall: Single<Response<U>>,
        firstResult: MutableLiveData<RetrofitResult<T>>,
        secondResult: MutableLiveData<RetrofitResult<U>>
) {
    Single.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                firstResult.subscribe(it.first)
                secondResult.subscribe(it.second)
            }, {
                firstResult.callError(it)
                secondResult.callError(it)
            }).addTo(this)
}

//

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Maybe<Response<T>>,
        secondCall: Maybe<Response<U>>,
        onError: (throwable: Throwable) -> Unit,
        onFirstCall: (model: Response<T>) -> Unit,
        onSecondCall: (model: Response<U>) -> Unit
) {
    Maybe.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                onFirstCall(it.first)
                onSecondCall(it.second)
            }, {
                onError(it)
            }).addTo(this)
}

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Maybe<Response<T>>,
        secondCall: Maybe<Response<U>>,
        firstResult: MutableLiveData<RetrofitResult<T>>,
        secondResult: MutableLiveData<RetrofitResult<U>>,
        onError: (throwable: Throwable) -> Unit
) {
    Maybe.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                firstResult.subscribe(it.first)
                secondResult.subscribe(it.second)
            }, {
                onError(it)
            }).addTo(this)
}

fun <T, U> CompositeDisposable.parallelApiCall(
        firstCall: Maybe<Response<T>>,
        secondCall: Maybe<Response<U>>,
        firstResult: MutableLiveData<RetrofitResult<T>>,
        secondResult: MutableLiveData<RetrofitResult<U>>
) {
    Maybe.zip(
            firstCall.subscribeOn(ioThreadScheduler),
            secondCall.subscribeOn(ioThreadScheduler),
            BiFunction { firstResponse: Response<T>, secondResponse: Response<U> ->
                Pair(firstResponse, secondResponse)
            }
    ).observeOn(mainThreadScheduler)
            .subscribe({
                firstResult.subscribe(it.first)
                secondResult.subscribe(it.second)
            }, {
                firstResult.callError(it)
                secondResult.callError(it)
            }).addTo(this)
}

//post api calls


/**
 *
 * @receiver Flowable<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param dropBackPressure Boolean
 * @param includeEmptyData Boolean
 */
fun <T : Response<R>, R> Flowable<T>?.makeApiCallPost(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        dropBackPressure: Boolean = false,
        includeEmptyData: Boolean = false
) {
    result.loadingPost()

    this?.let { call ->

        if (dropBackPressure) {
            call.onBackpressureDrop()
        }
        call.observeOn(mainThreadScheduler)
                .subscribe({
                    result.subscribePost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                }).addTo(compositeDisposable)
    }

}


fun <T : Response<R>, R> Flowable<T>?.makeApiCallListPost(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        dropBackPressure: Boolean = false,
        includeEmptyData: Boolean = true
) {
    result.loadingPost()

    this?.let { call ->

        if (dropBackPressure) {
            call.onBackpressureDrop()
        }
        call.observeOn(mainThreadScheduler)
                .subscribe({
                    result.subscribeListPost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                }).addTo(compositeDisposable)
    }

}

/**
 *
 * @receiver Single<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T : Response<R>, R> Single<T>?.makeApiCallPost(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.loadingPost()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribePost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)

}

fun <T : Response<R>, R> Single<T>?.makeApiCallListPost(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.loadingPost()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribePost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)

}

/**
 *
 * @receiver Observable<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T : Response<R>, R> Observable<T>?.makeApiCallPost(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.loadingPost()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribePost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)

}

fun <T : Response<R>, R> Observable<T>?.makeApiCallListPost(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.loadingPost()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribePost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)

}

/**
 *
 * @receiver Maybe<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T : Response<R>, R> Maybe<T>?.makeApiCallPost(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.loadingPost()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribePost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)

}

fun <T : Response<R>, R> Maybe<T>?.makeApiCallListPost(
        result: MutableLiveData<RetrofitResult<R>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.loadingPost()
    this?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribePost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)

}

/**
 *
 * @receiver CompositeDisposable
 * @param result MutableLiveData<RetrofitResult<T>>
 * @param dropBackPressure Boolean
 * @param includeEmptyData Boolean
 * @param function Function0<Flowable<Response<T>>?>
 */
fun <T> CompositeDisposable.makeApiCallPost(result: MutableLiveData<RetrofitResult<T>>, dropBackPressure: Boolean = false,
                                        includeEmptyData: Boolean = false, function: () -> Flowable<Response<T>>?) {
    result.loadingPost()
    val disposable = function()
            ?.observeOn(mainThreadScheduler)


    if (dropBackPressure) {
        disposable?.onBackpressureDrop()
                ?.subscribe({
                    result.subscribePost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                })
                ?.addTo(this)
    } else {
        disposable
                ?.subscribe({
                    result.subscribe(it, includeEmptyData)
                }, {
                    result.callError(it)
                })
                ?.addTo(this)
    }

}

fun <T> CompositeDisposable.makeApiCallListPost(result: MutableLiveData<RetrofitResult<T>>, dropBackPressure: Boolean = false,
                                            includeEmptyData: Boolean = true, function: () -> Flowable<Response<T>>?) {
    result.loadingPost()
    val disposable = function()
            ?.observeOn(mainThreadScheduler)


    if (dropBackPressure) {
        disposable?.onBackpressureDrop()
                ?.subscribe({
                    result.subscribeListPost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                })
                ?.addTo(this)
    } else {
        disposable
                ?.subscribe({
                    result.subscribeListPost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                })
                ?.addTo(this)
    }

}


/**
 *
 * @receiver CompositeDisposable
 * @param result MutableLiveData<RetrofitResult<T>>
 * @param includeEmptyData Boolean
 * @param function Function0<Single<Response<T>>?>
 */
fun <T> CompositeDisposable.makeApiCallSinglePost(result: MutableLiveData<RetrofitResult<T>>,
                                              includeEmptyData: Boolean = false, function: () -> Single<Response<T>>?) {
    result.loadingPost()
    function()
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribePost(it, includeEmptyData)
            }, {
                result.callErrorPost(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeApiCallListSinglePost(result: MutableLiveData<RetrofitResult<T>>,
                                                  includeEmptyData: Boolean = true, function: () -> Single<Response<T>>?) {
    result.loadingPost()
    function()
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribeListPost(it, includeEmptyData)
            }, {
                result.callErrorPost(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeApiCallMaybePost(result: MutableLiveData<RetrofitResult<T>>,
                                             includeEmptyData: Boolean = false, function: () -> Maybe<Response<T>>?) {
    result.loadingPost()
    function()
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribePost(it, includeEmptyData)
            }, {
                result.callErrorPost(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeApiCallListMaybePost(result: MutableLiveData<RetrofitResult<T>>,
                                                 includeEmptyData: Boolean = true, function: () -> Maybe<Response<T>>?) {
    result.loadingPost()
    function()
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribeListPost(it, includeEmptyData)
            }, {
                result.callErrorPost(it)
            })
            ?.addTo(this)
}


//db call post

/**
 *
 * @receiver Flowable<T>?
 * @param result MutableLiveData<DBResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param dropBackPressure Boolean
 * @param includeEmptyData Boolean
 */
fun <T> Flowable<T>?.makeDBCallPost(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        dropBackPressure: Boolean = false,
        includeEmptyData: Boolean = false
) {
    result.queryingPost()
    this?.let { call ->
        if (dropBackPressure) {
            call.onBackpressureDrop()
        }
        call.subscribeOn(ioThreadScheduler)
                .observeOn(mainThreadScheduler)
                .subscribe({
                    result.subscribePost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                }).addTo(compositeDisposable)
    }
}

fun <T> Flowable<T>?.makeDBCallListPost(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        dropBackPressure: Boolean = false,
        includeEmptyData: Boolean = true
) {
    result.queryingPost()
    this?.let { call ->
        if (dropBackPressure) {
            call.onBackpressureDrop()
        }
        call.subscribeOn(ioThreadScheduler)
                .observeOn(mainThreadScheduler)
                .subscribe({
                    result.subscribeListPost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                }).addTo(compositeDisposable)
    }
}

/**
 *
 * @receiver Single<T>?
 * @param result MutableLiveData<DBResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T> Single<T>?.makeDBCallPost(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.queryingPost()
    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribePost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)

}

fun <T> Single<T>?.makeDBCallListPost(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.queryingPost()
    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribeListPost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)

}

/**
 *
 * @receiver Observable<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T> Observable<T>?.makeDBCallPost(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.queryingPost()

    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribePost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)

}

fun <T> Observable<T>?.makeDBCallListPost(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.queryingPost()
    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribeListPost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)

}

/**
 *
 * @receiver Maybe<T>?
 * @param result MutableLiveData<RetrofitResult<R>>
 * @param compositeDisposable CompositeDisposable
 * @param includeEmptyData Boolean
 */
fun <T> Maybe<T>?.makeDBCallPost(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = false
) {
    result.queryingPost()
    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribeListPost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)
}

fun <T> Maybe<T>?.makeDBCallListPost(
        result: MutableLiveData<DBResult<T>>,
        compositeDisposable: CompositeDisposable,
        includeEmptyData: Boolean = true
) {
    result.queryingPost()

    this?.subscribeOn(ioThreadScheduler)?.observeOn(mainThreadScheduler)?.subscribe({
        result.subscribeListPost(it, includeEmptyData)
    }, {
        result.callErrorPost(it)
    })?.addTo(compositeDisposable)
}


fun <T> CompositeDisposable.makeDBCallPost(result: MutableLiveData<DBResult<T>>, dropBackPressure: Boolean = false,
                                       includeEmptyData: Boolean = false, function: () -> Flowable<T>?) {
    result.queryingPost()
    val disposable = function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)

    if (dropBackPressure) {
        disposable?.onBackpressureDrop()
                ?.subscribe({
                    result.subscribePost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                })
                ?.addTo(this)
    } else {
        disposable
                ?.subscribe({
                    result.subscribePost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                })
                ?.addTo(this)
    }

}

fun <T> CompositeDisposable.makeDBCallListPost(result: MutableLiveData<DBResult<T>>, dropBackPressure: Boolean = false,
                                           includeEmptyData: Boolean = false, function: () -> Flowable<T>?) {
    result.queryingPost()
    val disposable = function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)

    if (dropBackPressure) {
        disposable?.onBackpressureDrop()
                ?.subscribe({
                    result.subscribeListPost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                })
                ?.addTo(this)
    } else {
        disposable
                ?.subscribe({
                    result.subscribeListPost(it, includeEmptyData)
                }, {
                    result.callErrorPost(it)
                })
                ?.addTo(this)
    }

}

fun <T> CompositeDisposable.makeDBCallSinglePost(result: MutableLiveData<DBResult<T>>,
                                             includeEmptyData: Boolean = false, function: () -> Single<T>?) {
    result.queryingPost()
    function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribePost(it, includeEmptyData)
            }, {
                result.callErrorPost(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeDBCallListSinglePost(result: MutableLiveData<DBResult<T>>,
                                                 includeEmptyData: Boolean = true, function: () -> Single<T>?) {
    result.queryingPost()
    function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribeListPost(it, includeEmptyData)
            }, {
                result.callErrorPost(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeDBCallMaybePost(result: MutableLiveData<DBResult<T>>,
                                            includeEmptyData: Boolean = false, function: () -> Maybe<T>?) {
    result.queryingPost()
    function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribeListPost(it, includeEmptyData)
            }, {
                result.callErrorPost(it)
            })
            ?.addTo(this)
}

fun <T> CompositeDisposable.makeDBCallListMaybePost(result: MutableLiveData<DBResult<T>>,
                                                includeEmptyData: Boolean = true, function: () -> Maybe<T>?) {
    result.queryingPost()
    function()
            ?.subscribeOn(ioThreadScheduler)
            ?.observeOn(mainThreadScheduler)
            ?.subscribe({
                result.subscribeListPost(it, includeEmptyData)
            }, {
                result.callErrorPost(it)
            })
            ?.addTo(this)
}

fun CompositeDisposable.clearAndDispose() {
    clear()
    dispose()
}