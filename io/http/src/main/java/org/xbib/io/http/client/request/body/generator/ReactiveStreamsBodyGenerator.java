package org.xbib.io.http.client.request.body.generator;

import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.xbib.io.http.client.request.body.Body;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ReactiveStreamsBodyGenerator implements FeedableBodyGenerator {
    private static final ByteBuffer EMPTY = ByteBuffer.wrap("".getBytes());

    private final Publisher<ByteBuffer> publisher;
    private final FeedableBodyGenerator feedableBodyGenerator;
    private final AtomicReference<FeedListener> feedListener = new AtomicReference<>(null);

    public ReactiveStreamsBodyGenerator(Publisher<ByteBuffer> publisher) {
        this.publisher = publisher;
        this.feedableBodyGenerator = new UnboundedQueueFeedableBodyGenerator();
    }

    public Publisher<ByteBuffer> getPublisher() {
        return this.publisher;
    }

    @Override
    public boolean feed(ByteBuffer buffer, boolean isLast) throws Exception {
        return feedableBodyGenerator.feed(buffer, isLast);
    }

    @Override
    public void setListener(FeedListener listener) {
        feedListener.set(listener);
        feedableBodyGenerator.setListener(listener);
    }

    @Override
    public Body createBody() {
        return new StreamedBody(publisher, feedableBodyGenerator);
    }

    private class StreamedBody implements Body {
        private final AtomicBoolean initialized = new AtomicBoolean(false);

        private final SimpleSubscriber subscriber;
        private final Body body;

        public StreamedBody(Publisher<ByteBuffer> publisher, FeedableBodyGenerator bodyGenerator) {
            this.body = bodyGenerator.createBody();
            this.subscriber = new SimpleSubscriber(bodyGenerator);
        }

        @Override
        public void close() throws IOException {
            body.close();
        }

        @Override
        public long getContentLength() {
            return body.getContentLength();
        }

        @Override
        public BodyState transferTo(ByteBuf target) throws IOException {
            if (initialized.compareAndSet(false, true)) {
                publisher.subscribe(subscriber);
            }

            return body.transferTo(target);
        }
    }

    private class SimpleSubscriber implements Subscriber<ByteBuffer> {

        private final FeedableBodyGenerator feeder;
        private volatile Subscription subscription;

        public SimpleSubscriber(FeedableBodyGenerator feeder) {
            this.feeder = feeder;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (s == null) {
                throw null;
            }

            // If someone has made a mistake and added this Subscriber multiple times, let's handle it gracefully
            if (this.subscription != null) {
                s.cancel(); // Cancel the additional subscription
            } else {
                subscription = s;
                subscription.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(ByteBuffer t) {
            if (t == null) {
                throw null;
            }
            try {
                feeder.feed(t, false);
            } catch (Exception e) {
                subscription.cancel();
            }
        }

        @Override
        public void onError(Throwable t) {
            if (t == null) {
                throw null;
            }
            FeedListener listener = feedListener.get();
            if (listener != null) {
                listener.onError(t);
            }
        }

        @Override
        public void onComplete() {
            try {
                feeder.feed(EMPTY, true);
            } catch (Exception e) {
                this.subscription.cancel();
            }
        }
    }
}