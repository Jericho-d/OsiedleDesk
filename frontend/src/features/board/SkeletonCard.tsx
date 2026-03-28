import styles from './SkeletonCard.module.css';

export function SkeletonCard() {
  return (
    <div className={styles.skeleton}>
      <div className={styles.shimmer} />
      <div className={styles.title} />
      <div className={styles.title} style={{ width: '60%' }} />
      <div className={styles.meta}>
        <div className={styles.id} />
        <div className={styles.badge} />
      </div>
    </div>
  );
}
