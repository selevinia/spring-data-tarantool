package org.springframework.data.tarantool.repository;

import java.util.StringJoiner;

public class Sort {
    private final Direction direction;

    private Sort(Direction direction) {
        this.direction = direction;
    }

    public static Sort asc() {
        return new Sort(Direction.ASC);
    }

    public static Sort desc() {
        return new Sort(Direction.DESC);
    }

    /**
     * Returns whether sorting will be ascending.
     *
     * @return true if sorting will be ascending
     */
    public boolean isAscending() {
        return this.direction.isAscending();
    }

    /**
     * Returns whether sorting will be descending.
     *
     * @return true if sorting will be descending
     */
    public boolean isDescending() {
        return this.direction.isDescending();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Sort.class.getSimpleName() + "[", "]")
                .add("direction=" + direction)
                .toString();
    }

    /**
     * Enumeration for sort directions.
     *
     * @author Alexander Rublev
     */
    public enum Direction {
        ASC, DESC;

        /**
         * Returns whether the direction is ascending.
         *
         * @return true if direction is ascending
         */
        public boolean isAscending() {
            return this.equals(ASC);
        }

        /**
         * Returns whether the direction is descending.
         *
         * @return true if direction is descending
         */
        public boolean isDescending() {
            return this.equals(DESC);
        }
    }
}
